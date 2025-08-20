package unicam.progetto_filiera_springboot.application.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.mapper.ProdottoMapper;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.domain.event.EventPublisher;
import unicam.progetto_filiera_springboot.domain.event.ProdottoInviatoAlCuratore;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;
import unicam.progetto_filiera_springboot.domain.model.Utente;
import unicam.progetto_filiera_springboot.infrastructure.storage.FileStorageStrategy;
import unicam.progetto_filiera_springboot.repository.ProdottoRepository;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;
import unicam.progetto_filiera_springboot.controller.error.UploadException;

import java.io.IOException;
import java.util.List;

@Service
public class ProdottoServiceImpl implements ProdottoService {

    private final ProdottoRepository prodottoRepository;
    private final UtenteRepository utenteRepository;
    private final FileStorageStrategy storage;
    private final EventPublisher eventPublisher;

    public ProdottoServiceImpl(ProdottoRepository prodottoRepository,
                               UtenteRepository utenteRepository,
                               @Qualifier("fileStorageStrategy") FileStorageStrategy storage,
                               EventPublisher eventPublisher) {
        this.prodottoRepository = prodottoRepository;
        this.utenteRepository = utenteRepository;
        this.storage = storage;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Crea il prodotto con stato IN_ATTESA, carica foto e certificati obbligatori
     * e notifica l’evento al Curatore (Observer).
     */
    @Override
    @Transactional
    public ProdottoResponse creaProdottoConFile(ProdottoForm req,
                                                String usernameCreatore,
                                                List<MultipartFile> foto,
                                                List<MultipartFile> certificati) {

        // Validazioni lato service (oltre a quelle @Valid del form)
        if (foto == null || foto.isEmpty()) {
            throw new ValidationException("Devi caricare almeno una foto.");
        }
        if (certificati == null || certificati.isEmpty()) {
            throw new ValidationException("Devi caricare almeno un certificato.");
        }

        Utente creatore = utenteRepository.findById(usernameCreatore)
                .orElseThrow(() -> new IllegalArgumentException("Utente inesistente: " + usernameCreatore));

        // Unicità per nome + creatore
        if (prodottoRepository.existsByNomeAndCreatoDa_Username(req.getNome(), creatore.getUsername())) {
            throw new ValidationException("Esiste già un prodotto con lo stesso nome per questo utente.");
        }


        // 1) Crea Prodotto (l'entity imposta lo stato IN_ATTESA nel costruttore)
        Prodotto p = new Prodotto(
                req.getNome(),
                req.getDescrizione(),
                req.getQuantita(),
                req.getPrezzo(),
                req.getIndirizzo(),
                creatore
        );
        Prodotto saved = prodottoRepository.save(p);

        // 2) Carica FILE (Strategy)
        try {
            var fotoSaved = storage.store(foto, "prodotti/" + saved.getId() + "/foto");
            var certSaved = storage.store(certificati, "prodotti/" + saved.getId() + "/certificati");

            saved.setFoto(String.join(",", fotoSaved));
            saved.setCertificati(String.join(",", certSaved));
            saved = prodottoRepository.save(saved);

        } catch (IllegalArgumentException e) {
            // errori di validazione strategy (tipo/size)
            throw new UploadException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploadException("Errore durante il salvataggio dei file.", e);
        }

        // 3) Observer: notifica “inviato al Curatore”
        eventPublisher.publish(new ProdottoInviatoAlCuratore(saved.getId(), creatore.getUsername()));

        return ProdottoMapper.toResponse(saved);
    }

    /**
     * Upload foto aggiuntive (opzionale post-creazione)
     */
    @Override
    @Transactional
    public ProdottoResponse uploadFoto(Long prodottoId, List<MultipartFile> files) {
        return doUpload(prodottoId, files, true);
    }

    /**
     * Upload certificati aggiuntivi (opzionale post-creazione)
     */
    @Override
    @Transactional
    public ProdottoResponse uploadCertificati(Long prodottoId, List<MultipartFile> files) {
        return doUpload(prodottoId, files, false);
    }

    // ------------------------ helper privati ------------------------

    private ProdottoResponse doUpload(Long prodottoId, List<MultipartFile> files, boolean isFoto) {
        if (files == null || files.isEmpty()) {
            throw new UploadException("Nessun file selezionato.");
        }

        Prodotto p = prodottoRepository.findById(prodottoId)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto inesistente: id=" + prodottoId));

        String subfolder = "prodotti/" + prodottoId + (isFoto ? "/foto" : "/certificati");

        List<String> saved;
        try {
            saved = storage.store(files, subfolder);
        } catch (IllegalArgumentException e) {
            throw new UploadException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploadException("Errore di IO durante il salvataggio dei file.", e);
        }

        String csv = String.join(",", saved);
        if (isFoto) {
            p.setFoto(appendCsv(p.getFoto(), csv));
        } else {
            p.setCertificati(appendCsv(p.getCertificati(), csv));
        }

        Prodotto updated = prodottoRepository.save(p);
        return ProdottoMapper.toResponse(updated);
    }

    private String appendCsv(String existing, String toAdd) {
        if (existing == null || existing.isBlank()) return toAdd;
        if (toAdd == null || toAdd.isBlank()) return existing;
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

}
