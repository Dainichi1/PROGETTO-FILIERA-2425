package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.observer.EliminazioneProfiloNotifier;
import unicam.filiera.repository.RichiestaEliminazioneProfiloRepository;
import unicam.filiera.repository.UtenteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EliminazioneProfiloServiceImpl implements EliminazioneProfiloService {

    private static final Logger log = LoggerFactory.getLogger(EliminazioneProfiloServiceImpl.class);

    private final RichiestaEliminazioneProfiloRepository richiestaRepo;
    private final UtenteRepository utenteRepo;
    private final EliminazioneProfiloNotifier notifier;

    @Autowired
    public EliminazioneProfiloServiceImpl(RichiestaEliminazioneProfiloRepository richiestaRepo,
                                          UtenteRepository utenteRepo,
                                          EliminazioneProfiloNotifier notifier) {
        this.richiestaRepo = richiestaRepo;
        this.utenteRepo = utenteRepo;
        this.notifier = notifier;
    }

    @Override
    @Transactional
    public void inviaRichiestaEliminazione(RichiestaEliminazioneProfiloDto dto) {
        log.info("[Eliminazione] Richiesta ricevuta per username={}", dto.getUsername());

        boolean giaPresente = richiestaRepo
                .findFirstByUsernameAndStatoOrderByDataRichiestaDesc(
                        dto.getUsername(), StatoRichiestaEliminazioneProfilo.IN_ATTESA
                ).isPresent();

        if (giaPresente) {
            throw new IllegalStateException("Esiste già una richiesta IN_ATTESA per l’utente " + dto.getUsername());
        }

        RichiestaEliminazioneProfiloEntity entity = new RichiestaEliminazioneProfiloEntity();
        entity.setUsername(dto.getUsername());
        entity.setStato(StatoRichiestaEliminazioneProfilo.IN_ATTESA);
        entity.setDataRichiesta(LocalDateTime.now());

        richiestaRepo.save(entity);
        log.info("[Eliminazione] Richiesta salvata: id={}, username={}, stato={}",
                entity.getId(), entity.getUsername(), entity.getStato());
    }

    @Override
    public List<RichiestaEliminazioneProfiloDto> getRichiesteByStato(StatoRichiestaEliminazioneProfilo stato) {
        log.debug("[Eliminazione] Recupero richieste con stato={}", stato);
        return richiestaRepo.findByStato(stato).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<RichiestaEliminazioneProfiloDto> getRichiesteByUtente(String username) {
        log.debug("[Eliminazione] Recupero richieste per utente={}", username);
        return richiestaRepo.findByUsername(username).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public Optional<RichiestaEliminazioneProfiloEntity> findEntityById(Long id) {
        log.debug("[Eliminazione] findEntityById id={}", id);
        return richiestaRepo.findById(id);
    }

    @Override
    @Transactional
    public void aggiornaStato(Long id, StatoRichiestaEliminazioneProfilo nuovoStato) {
        log.info("[Eliminazione] Aggiornamento stato richiesta id={} → {}", id, nuovoStato);

        RichiestaEliminazioneProfiloEntity entity = richiestaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata"));

        entity.setStato(nuovoStato);
        richiestaRepo.save(entity);

        log.info("[Eliminazione] Stato aggiornato: id={}, username={}, nuovoStato={}",
                entity.getId(), entity.getUsername(), entity.getStato());

        // notifiche + eliminazione utente se approvata
        if (nuovoStato == StatoRichiestaEliminazioneProfilo.RIFIUTATA) {
            notifier.notificaRifiutata(mapToDomain(entity));
        } else if (nuovoStato == StatoRichiestaEliminazioneProfilo.APPROVATA) {
            utenteRepo.deleteById(entity.getUsername());
            notifier.notificaApprovata(mapToDomain(entity));
        }
    }

    // ================= Helpers =================
    private RichiestaEliminazioneProfilo mapToDomain(RichiestaEliminazioneProfiloEntity e) {
        return new RichiestaEliminazioneProfilo.Builder()
                .id(e.getId())
                .username(e.getUsername())
                .stato(e.getStato())
                .dataRichiesta(e.getDataRichiesta())
                .build();
    }

    private RichiestaEliminazioneProfiloDto mapToDto(RichiestaEliminazioneProfiloEntity e) {
        return RichiestaEliminazioneProfiloDto.builder()
                .id(e.getId())
                .username(e.getUsername())
                .stato(e.getStato().name())
                .dataRichiesta(e.getDataRichiesta())
                .build();
    }
}
