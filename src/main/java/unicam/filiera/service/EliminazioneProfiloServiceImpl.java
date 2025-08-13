package unicam.filiera.service;

import unicam.filiera.dao.RichiestaEliminazioneProfiloDAO;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Implementazione della logica per le richieste di eliminazione profilo.
 */
public class EliminazioneProfiloServiceImpl implements EliminazioneProfiloService {

    private final RichiestaEliminazioneProfiloDAO dao;

    public EliminazioneProfiloServiceImpl(RichiestaEliminazioneProfiloDAO dao) {
        this.dao = Objects.requireNonNull(dao, "DAO non può essere null");
    }

    @Override
    public void inviaRichiestaEliminazione(RichiestaEliminazioneProfiloDto dto) {
        Objects.requireNonNull(dto, "DTO non può essere null");
        Objects.requireNonNull(dto.getUsername(), "Username non può essere null");

        // 1. Verifica se esiste già una richiesta in attesa
        boolean richiestaGiaPresente = dao.findByUsername(dto.getUsername())
                .stream()
                .anyMatch(r -> r.getStato() == StatoRichiestaEliminazioneProfilo.IN_ATTESA);

        if (richiestaGiaPresente) {
            throw new IllegalStateException(
                    "Esiste già una richiesta di eliminazione IN_ATTESA per l'utente: " + dto.getUsername()
            );
        }

        // 2. Mappa DTO → Entity
        RichiestaEliminazioneProfilo richiesta = new RichiestaEliminazioneProfilo(
                dto.getUsername(),
                StatoRichiestaEliminazioneProfilo.IN_ATTESA,
                LocalDateTime.now()
        );

        // 3. Salvataggio con gestione errore
        if (!dao.save(richiesta)) {
            throw new RuntimeException("Errore durante il salvataggio della richiesta di eliminazione profilo");
        }
    }

    @Override
    public List<RichiestaEliminazioneProfilo> getRichiesteByStato(StatoRichiestaEliminazioneProfilo stato) {
        return dao.findByStato(stato);
    }

    @Override
    public List<RichiestaEliminazioneProfilo> getRichiesteByUtente(String username) {
        return dao.findByUsername(username);
    }
}
