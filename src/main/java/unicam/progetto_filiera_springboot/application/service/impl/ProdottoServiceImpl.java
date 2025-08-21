package unicam.progetto_filiera_springboot.application.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.mapper.ProdottoMapper;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.controller.error.UploadException;
import unicam.progetto_filiera_springboot.domain.event.EventPublisher;
import unicam.progetto_filiera_springboot.domain.event.ProdottoInviatoAlCuratore;
import unicam.progetto_filiera_springboot.domain.factory.ProdottoFactory;
import unicam.progetto_filiera_springboot.domain.model.Item;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;
import unicam.progetto_filiera_springboot.domain.model.StatoProdotto;
import unicam.progetto_filiera_springboot.domain.model.Utente;
import unicam.progetto_filiera_springboot.infrastructure.storage.FileStorageStrategy;
import unicam.progetto_filiera_springboot.repository.ProdottoRepository;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProdottoServiceImpl implements ProdottoService {

    private final ProdottoRepository prodottoRepository;
    private final UtenteRepository utenteRepository;
    private final FileStorageStrategy storage;
    private final EventPublisher eventPublisher;
    private final ProdottoFactory prodottoFactory; // <-- Factory

    public ProdottoServiceImpl(ProdottoRepository prodottoRepository,
                               UtenteRepository utenteRepository,
                               @Qualifier("localFileStorageStrategy") FileStorageStrategy storage,
                               EventPublisher eventPublisher,
                               ProdottoFactory prodottoFactory) {
        this.prodottoRepository = prodottoRepository;
        this.utenteRepository = utenteRepository;
        this.storage = storage;
        this.eventPublisher = eventPublisher;
        this.prodottoFactory = prodottoFactory;
    }

    @Override
    @Transactional
    public ProdottoResponse creaProdottoConFile(ProdottoForm req,
                                                String usernameCreatore,
                                                List<MultipartFile> foto,
                                                List<MultipartFile> certificati) {

        if (foto == null || foto.isEmpty()) {
            throw new ValidationException("Devi caricare almeno una foto.");
        }
        if (certificati == null || certificati.isEmpty()) {
            throw new ValidationException("Devi caricare almeno un certificato.");
        }

        Utente creatore = utenteRepository.findById(usernameCreatore)
                .orElseThrow(() -> new IllegalArgumentException("Utente inesistente: " + usernameCreatore));

        if (prodottoRepository.existsByNomeAndCreatoDa_Username(req.getNome(), creatore.getUsername())) {
            throw new ValidationException("Esiste già un prodotto con lo stesso nome per questo utente.");
        }

        // 1) Crea entità tramite FACTORY (ritorna Item) – stato IN_ATTESA gestito dal costruttore dell'entity
        //    Certificati/foto verranno impostati dopo l'upload, quindi qui passiamo null.
        Item item = prodottoFactory.creaProdotto(
                req.getNome(),
                req.getDescrizione(),
                req.getQuantita(),
                req.getPrezzo(),
                req.getIndirizzo(),
                creatore,
                null,
                null
        );
        Prodotto p = (Prodotto) item; // cast sicuro: la factory concreta istanzia Prodotto
        Prodotto saved = prodottoRepository.save(p);

        // 2) Carica file e salva URL pubblici nei campi CSV
        try {
            var fotoSaved = storage.store(foto, "prodotti/" + saved.getId() + "/foto");
            var certSaved = storage.store(certificati, "prodotti/" + saved.getId() + "/certificati");

            var fotoUrls = filenamesToPublicUrls(saved.getId(), "foto", fotoSaved);
            var certUrls = filenamesToPublicUrls(saved.getId(), "certificati", certSaved);

            saved.setFoto(joinCsv(fotoUrls));
            saved.setCertificati(joinCsv(certUrls));
            saved = prodottoRepository.save(saved);

        } catch (IllegalArgumentException e) {
            throw new UploadException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploadException("Errore durante il salvataggio dei file.", e);
        }

        // 3) Observer: notifica invio al Curatore
        eventPublisher.publish(new ProdottoInviatoAlCuratore(saved.getId(), creatore.getUsername()));

        return ProdottoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProdottoResponse uploadFoto(Long prodottoId, List<MultipartFile> files) {
        return doUpload(prodottoId, files, true);
    }

    @Override
    @Transactional
    public ProdottoResponse uploadCertificati(Long prodottoId, List<MultipartFile> files) {
        return doUpload(prodottoId, files, false);
    }

    // Upload incrementale: converte i nuovi filename in URL e li appende al CSV esistente
    private ProdottoResponse doUpload(Long prodottoId, List<MultipartFile> files, boolean isFoto) {
        if (files == null || files.isEmpty()) {
            throw new UploadException("Nessun file selezionato.");
        }

        Prodotto p = prodottoRepository.findById(prodottoId)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + prodottoId));

        String subfolder = "prodotti/" + prodottoId + (isFoto ? "/foto" : "/certificati");

        List<String> savedFilenames;
        try {
            savedFilenames = storage.store(files, subfolder);
        } catch (IllegalArgumentException e) {
            throw new UploadException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploadException("Errore di IO durante il salvataggio dei file.", e);
        }

        if (isFoto) {
            var urlsToAdd = filenamesToPublicUrls(prodottoId, "foto", savedFilenames);
            p.setFoto(appendCsv(p.getFoto(), joinCsv(urlsToAdd)));
        } else {
            var urlsToAdd = filenamesToPublicUrls(prodottoId, "certificati", savedFilenames);
            p.setCertificati(appendCsv(p.getCertificati(), joinCsv(urlsToAdd)));
        }
        if (p.getStato() == StatoProdotto.APPROVATO /* || p.getStato() == PUBLISHED */) {
            throw new IllegalStateException("L'item non è più modificabile in questo stato.");
        }


        Prodotto updated = prodottoRepository.save(p);
        return ProdottoMapper.toResponse(updated);
    }

    private String appendCsv(String existing, String toAdd) {
        if (toAdd == null || toAdd.isBlank()) return existing;
        if (existing == null || existing.isBlank()) return toAdd;
        return existing + "," + toAdd;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdottoResponse> prodottiDi(String usernameCreatore) {
        return prodottoRepository
                .findByCreatoDa_UsernameOrderByCreatedAtDesc(usernameCreatore)
                .stream()
                .map(ProdottoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdottoResponse> listInAttesa() {
        return prodottoRepository.findByStato(StatoProdotto.IN_ATTESA)
                .stream()
                .map(ProdottoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void approve(Long id) {
        Prodotto p = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + id));

        if (p.getStato() != StatoProdotto.IN_ATTESA) {
            throw new IllegalStateException("Prodotto non in stato IN_ATTESA: id=" + id);
        }

        prodottoRepository.updateStatoAndCommento(id, StatoProdotto.APPROVATO, null);
    }


    @Override
    @Transactional
    public void reject(Long id, Optional<String> commento) {
        Prodotto p = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + id));
        if (p.getStato() != StatoProdotto.IN_ATTESA) {
            throw new IllegalStateException("Prodotto non in stato IN_ATTESA: id=" + id);
        }
        prodottoRepository.updateStatoAndCommento(
                id,
                StatoProdotto.RIFIUTATO,
                commento.filter(s -> !s.isBlank()).orElse(null)
        );
    }

    // ===== Helpers per URL pubblici =====
    private List<String> filenamesToPublicUrls(Long prodottoId, String tipo, List<String> filenames) {
        // tipo = "foto" | "certificati"
        // Esempio URL: /files/prodotti/12/foto/abcd-123.jpg
        return filenames.stream()
                .map(fn -> "/files/prodotti/" + prodottoId + "/" + tipo + "/" + fn)
                .toList();
    }

    private String joinCsv(List<String> list) {
        return (list == null || list.isEmpty()) ? "" : String.join(",", list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdottoResponse> listApprovati() {
        return prodottoRepository.findByStato(StatoProdotto.APPROVATO)
                .stream()
                .map(ProdottoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProdottoResponse findByIdAndOwner(Long id, String username) {
        var p = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + id));

        if (p.getCreatoDa() == null || p.getCreatoDa().getUsername() == null
                || !p.getCreatoDa().getUsername().equals(username)) {
            throw new IllegalArgumentException("Operazione non consentita per questo utente.");
        }
        return ProdottoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEliminabile(Long id, String username) {
        var p = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + id));

        if (p.getCreatoDa() == null || p.getCreatoDa().getUsername() == null
                || !p.getCreatoDa().getUsername().equals(username)) {
            throw new IllegalArgumentException("Operazione non consentita per questo utente.");
        }
        return p.getStato() != StatoProdotto.APPROVATO;
    }

    @Override
    @Transactional
    public void elimina(Long id, String username) {
        var p = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + id));

        if (p.getCreatoDa() == null || p.getCreatoDa().getUsername() == null
                || !p.getCreatoDa().getUsername().equals(username)) {
            throw new IllegalArgumentException("Operazione non consentita per questo utente.");
        }
        if (p.getStato() == StatoProdotto.APPROVATO) {
            throw new IllegalStateException("Puoi eliminare solo prodotti con stato \"In Attesa\" o \"Rifiutato\".");
        }



        prodottoRepository.deleteById(id);
    }
}
