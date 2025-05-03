// -------- VisitaInvitoServiceImpl.java --------
package unicam.filiera.service;

import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.observer.VisitaInvitoNotifier;
import unicam.filiera.dao.VisitaInvitoDAO;
import unicam.filiera.dao.JdbcVisitaInvitoDAO;
import unicam.filiera.util.ValidatoreVisitaInvito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementazione JDBC di VisitaInvitoService.
 */
public class VisitaInvitoServiceImpl implements VisitaInvitoService {
    private final VisitaInvitoDAO dao;
    private final VisitaInvitoNotifier notifier;

    /** Iniezione di dipendenza (utile per i test) */
    public VisitaInvitoServiceImpl(VisitaInvitoDAO dao) {
        this.dao      = dao;
        this.notifier = VisitaInvitoNotifier.getInstance();
    }

    /** Costruttore di convenienza per l’app reale */
    public VisitaInvitoServiceImpl() {
        this(JdbcVisitaInvitoDAO.getInstance());
    }

    @Override
    public void creaVisitaInvito(VisitaInvitoDto dto, String organizzatore) {
        // 1) validazione
        LocalDateTime inizio = LocalDate.parse(dto.getDataInizioTxt()).atStartOfDay();
        LocalDateTime fine   = LocalDate.parse(dto.getDataFineTxt()).atStartOfDay();
        double prezzo         = Double.parseDouble(dto.getPrezzoTxt());
        int minPar            = Integer.parseInt(dto.getMinPartecipantiTxt());

        ValidatoreVisitaInvito.validaDate(inizio, fine);
        ValidatoreVisitaInvito.validaLuogo(dto.getIndirizzo());
        ValidatoreVisitaInvito.validaPrezzo(prezzo);
        ValidatoreVisitaInvito.validaMinPartecipanti(minPar);
        ValidatoreVisitaInvito.validaDestinatari(dto.getDestinatari());

        // 2) costruzione del dominio già in stato PUBBLICATA
        VisitaInvito vi = new VisitaInvito.Builder()
                .id(0)  // verrà assegnato dal DB
                .dataInizio(inizio)
                .dataFine(fine)
                .prezzo(prezzo)
                .descrizione(dto.getDescrizione())
                .indirizzo(dto.getIndirizzo())
                .organizzatore(organizzatore)
                .numeroMinPartecipanti(minPar)
                .destinatari(dto.getDestinatari())
                .stato(StatoEvento.PUBBLICATA)
                .build();

        // 3) persistenza
        if (!dao.save(vi)) {
            throw new RuntimeException("Errore durante il salvataggio della visita su invito");
        }

        // 4) notifica tutti gli osservatori
        notifier.notificaTutti(vi, "VISITA_INVITO_PUBBLICATA");
    }

    @Override
    public List<VisitaInvito> getVisiteCreateDa(String organizzatore) {
        return dao.findByOrganizzatore(organizzatore);
    }
}
