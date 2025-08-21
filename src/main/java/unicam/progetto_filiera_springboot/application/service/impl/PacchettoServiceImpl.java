package unicam.progetto_filiera_springboot.application.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.mapper.PacchettoMapper;
import unicam.progetto_filiera_springboot.application.mapper.ProdottoMapper;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;
import unicam.progetto_filiera_springboot.controller.error.UploadException;
import unicam.progetto_filiera_springboot.domain.event.EventPublisher;
import unicam.progetto_filiera_springboot.domain.event.PacchettoInviatoAlCuratore;
import unicam.progetto_filiera_springboot.domain.factory.PacchettoFactory;
import unicam.progetto_filiera_springboot.domain.model.Pacchetto;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;
import unicam.progetto_filiera_springboot.domain.model.StatoPacchetto;
import unicam.progetto_filiera_springboot.domain.model.StatoProdotto;
import unicam.progetto_filiera_springboot.domain.model.Utente;
import unicam.progetto_filiera_springboot.infrastructure.storage.FileStorageStrategy;
import unicam.progetto_filiera_springboot.repository.PacchettoRepository;
import unicam.progetto_filiera_springboot.repository.ProdottoRepository;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
public class PacchettoServiceImpl implements PacchettoService {

    private final PacchettoRepository pacchettoRepository;
    private final ProdottoRepository prodottoRepository;
    private final UtenteRepository utenteRepository;
    private final FileStorageStrategy storage;
    private final EventPublisher eventPublisher;
    private final PacchettoFactory pacchettoFactory;

    public PacchettoServiceImpl(PacchettoRepository pacchettoRepository,
                                ProdottoRepository prodottoRepository,
                                UtenteRepository utenteRepository,
                                @Qualifier("localFileStorageStrategy") FileStorageStrategy storage,
                                EventPublisher eventPublisher,
                                PacchettoFactory pacchettoFactory) {
        this.pacchettoRepository = pacchettoRepository;
        this.prodottoRepository = prodottoRepository;
        this.utenteRepository = utenteRepository;
        this.storage = storage;
        this.eventPublisher = eventPublisher;
        this.pacchettoFactory = pacchettoFactory;
    }

    // ================== CREATE ==================
    @Override
    @Transactional
    public PacchettoResponse creaPacchettoConFile(PacchettoForm form,
                                                  String usernameDistributore,
                                                  List<MultipartFile> foto,
                                                  List<MultipartFile> certificati) {

        var prodottiSelezionati = form.getProdottiIds();

        // --- vincoli base su input ---
        if (prodottiSelezionati == null || prodottiSelezionati.isEmpty()) {
            throw new ValidationException("Seleziona almeno 2 prodotti.");
        }
        // prodotti distinti
        var ids = new HashSet<>(prodottiSelezionati);
        if (ids.size() < 2) {
            throw new ValidationException("Seleziona almeno 2 prodotti distinti.");
        }
        if (foto == null || foto.isEmpty()) {
            throw new ValidationException("Devi caricare almeno una foto.");
        }
        if (certificati == null || certificati.isEmpty()) {
            throw new ValidationException("Devi caricare almeno un certificato.");
        }

        Utente distributore = utenteRepository.findById(usernameDistributore)
                .orElseThrow(() -> new IllegalArgumentException("Distributore inesistente: " + usernameDistributore));

        // Carica i prodotti selezionati (in base agli ID distinti)
        List<Prodotto> prodotti = prodottoRepository.findAllById(ids);
        // esistenza completa
        if (prodotti.size() != ids.size()) {
            throw new ValidationException("Alcuni prodotti selezionati non esistono.");
        }
        // tutti già pubblicati/approvati per il marketplace
        boolean allPubblicati = prodotti.stream()
                .allMatch(pr -> pr.getStato() == StatoProdotto.APPROVATO /* oppure PUBLISHED se lo introduci */);
        if (!allPubblicati) {
            throw new ValidationException("Tutti i prodotti devono essere già pubblicati nel Marketplace.");
        }

        // 1) Crea l'entity tramite FACTORY (CSV null: li setteremo dopo l'upload)
        Pacchetto pacchetto = (Pacchetto) pacchettoFactory.creaPacchetto(
                form.getNome(),
                form.getDescrizione(),
                form.getQuantita(),
                form.getPrezzoTotale(),   // mappato sul campo 'prezzo' dell'entity
                form.getIndirizzo(),
                distributore,
                null,                     // certificatiCsv
                null                      // fotoCsv
        );

        // Associa i prodotti al pacchetto (manteniamo l'ordine di inserimento)
        pacchetto.setProdotti(new LinkedHashSet<>(prodotti));

        Pacchetto saved = pacchettoRepository.save(pacchetto);

        // 2) Upload file e salvataggio CSV URL pubblici
        try {
            var fotoSaved = storage.store(foto, "pacchetti/" + saved.getId() + "/foto");
            var certSaved = storage.store(certificati, "pacchetti/" + saved.getId() + "/certificati");

            var fotoUrls = filenamesToPublicUrls(saved.getId(), "foto", fotoSaved);
            var certUrls = filenamesToPublicUrls(saved.getId(), "certificati", certSaved);

            saved.setFoto(joinCsv(fotoUrls));
            saved.setCertificati(joinCsv(certUrls));
            saved = pacchettoRepository.save(saved);

        } catch (IllegalArgumentException e) {
            throw new UploadException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploadException("Errore durante il salvataggio dei file.", e);
        }

        // 3) Observer: notifica al Curatore
        eventPublisher.publish(new PacchettoInviatoAlCuratore(saved.getId(), distributore.getUsername()));

        return PacchettoMapper.toResponse(saved);
    }

    // ================== UPLOAD INCREMENTALE ==================
    @Override
    @Transactional
    public PacchettoResponse uploadFoto(Long pacchettoId, List<MultipartFile> files) {
        return doUpload(pacchettoId, files, true);
    }

    @Override
    @Transactional
    public PacchettoResponse uploadCertificati(Long pacchettoId, List<MultipartFile> files) {
        return doUpload(pacchettoId, files, false);
    }

    private PacchettoResponse doUpload(Long pacchettoId, List<MultipartFile> files, boolean isFoto) {
        if (files == null || files.isEmpty()) {
            throw new UploadException("Nessun file selezionato.");
        }

        Pacchetto p = pacchettoRepository.findById(pacchettoId)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto inesistente: id=" + pacchettoId));

        String subfolder = "pacchetti/" + pacchettoId + (isFoto ? "/foto" : "/certificati");

        List<String> savedFilenames;
        try {
            savedFilenames = storage.store(files, subfolder);
        } catch (IllegalArgumentException e) {
            throw new UploadException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploadException("Errore di IO durante il salvataggio dei file.", e);
        }

        if (isFoto) {
            var urlsToAdd = filenamesToPublicUrls(pacchettoId, "foto", savedFilenames);
            p.setFoto(appendCsv(p.getFoto(), String.join(",", urlsToAdd)));
        } else {
            var urlsToAdd = filenamesToPublicUrls(pacchettoId, "certificati", savedFilenames);
            p.setCertificati(appendCsv(p.getCertificati(), String.join(",", urlsToAdd)));
        }

        if (p.getStato() == StatoPacchetto.APPROVATO /* || p.getStato() == PUBLISHED */) {
            throw new IllegalStateException("L'item non è più modificabile in questo stato.");
        }


        Pacchetto updated = pacchettoRepository.save(p);
        return PacchettoMapper.toResponse(updated);
    }

    private String appendCsv(String existing, String toAdd) {
        if (toAdd == null || toAdd.isBlank()) return existing;
        if (existing == null || existing.isBlank()) return toAdd;
        return existing + "," + toAdd;
    }

    // ================== QUERY & APPROVAZIONE ==================
    @Override
    @Transactional(readOnly = true)
    public List<PacchettoResponse> pacchettiDi(String usernameDistributore) {
        return pacchettoRepository.findByCreatoDa_UsernameOrderByCreatedAtDesc(usernameDistributore)
                .stream()
                .map(PacchettoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacchettoResponse> listInAttesa() {
        return pacchettoRepository.findByStato(StatoPacchetto.IN_ATTESA)
                .stream()
                .map(PacchettoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void approve(Long id) {
        Pacchetto p = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto inesistente: id=" + id));

        if (p.getStato() != StatoPacchetto.IN_ATTESA) {
            throw new IllegalStateException("Pacchetto non in stato IN_ATTESA: id=" + id);
        }

        pacchettoRepository.updateStatoAndCommento(id, StatoPacchetto.APPROVATO, null);
    }


    @Override
    @Transactional
    public void reject(Long id, Optional<String> commento) {
        Pacchetto p = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto inesistente: id=" + id));
        if (p.getStato() != StatoPacchetto.IN_ATTESA) {
            throw new IllegalStateException("Pacchetto non in stato IN_ATTESA: id=" + id);
        }
        pacchettoRepository.updateStatoAndCommento(id, StatoPacchetto.RIFIUTATO, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacchettoResponse> listApprovati() {
        return pacchettoRepository.findByStato(StatoPacchetto.APPROVATO)
                .stream()
                .map(PacchettoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacchettoResponse findByIdAndOwner(Long id, String username) {
        var p = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto inesistente: id=" + id));

        if (p.getCreatoDa() == null || p.getCreatoDa().getUsername() == null
                || !p.getCreatoDa().getUsername().equals(username)) {
            throw new IllegalArgumentException("Operazione non consentita per questo utente.");
        }
        return PacchettoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEliminabile(Long id, String username) {
        var p = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto inesistente: id=" + id));

        if (p.getCreatoDa() == null || p.getCreatoDa().getUsername() == null
                || !p.getCreatoDa().getUsername().equals(username)) {
            throw new IllegalArgumentException("Operazione non consentita per questo utente.");
        }
        return p.getStato() != StatoPacchetto.APPROVATO;
    }

    @Override
    @Transactional
    public void elimina(Long id, String username) {
        var p = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto inesistente: id=" + id));

        if (p.getCreatoDa() == null || p.getCreatoDa().getUsername() == null
                || !p.getCreatoDa().getUsername().equals(username)) {
            throw new IllegalArgumentException("Operazione non consentita per questo utente.");
        }
        if (p.getStato() == StatoPacchetto.APPROVATO) {
            throw new IllegalStateException("Puoi eliminare solo pacchetti con stato \"In Attesa\" o \"Rifiutato\".");
        }


        pacchettoRepository.deleteById(id);

    }

    // ================== Helpers ==================
    private List<String> filenamesToPublicUrls(Long pacchettoId, String tipo, List<String> filenames) {
        return filenames.stream()
                .map(fn -> "/files/pacchetti/" + pacchettoId + "/" + tipo + "/" + fn)
                .toList();
    }

    private String joinCsv(List<String> list) {
        return (list == null || list.isEmpty()) ? "" : String.join(",", list);
    }
}
