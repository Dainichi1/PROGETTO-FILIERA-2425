package unicam.filiera.service;

import unicam.filiera.dao.RichiestaEliminazioneProfiloDAO;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementazione della logica per le richieste di eliminazione profilo.
 */
public class EliminazioneProfiloServiceImpl implements EliminazioneProfiloService {
    private final RichiestaEliminazioneProfiloDAO dao;

    public EliminazioneProfiloServiceImpl(RichiestaEliminazioneProfiloDAO dao) {
        this.dao = dao;
    }

    @Override
    public void inviaRichiestaEliminazione(RichiestaEliminazioneProfiloDto dto) {
        // 1. Verifica che non esista già una richiesta IN_ATTESA per questo utente
        List<RichiestaEliminazioneProfilo> richieste = dao.findByUsername(dto.getUsername());
        boolean richiestaGiaPresente = richieste.stream()
                .anyMatch(r -> r.getStato() == StatoRichiestaEliminazioneProfilo.IN_ATTESA);

        if (richiestaGiaPresente) {
            throw new IllegalStateException("Esiste già una richiesta di eliminazione IN_ATTESA per questo utente.");
        }

        // 2. Mapping DTO → Domain
        RichiestaEliminazioneProfilo richiesta = new RichiestaEliminazioneProfilo(
                dto.getUsername(),
                StatoRichiestaEliminazioneProfilo.IN_ATTESA,
                LocalDateTime.now()
        );

        // 3. Salvataggio
        boolean ok = dao.save(richiesta);
        if (!ok) throw new RuntimeException("Errore durante il salvataggio della richiesta.");
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
