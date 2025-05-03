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
import java.time.format.DateTimeParseException;
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
        // 1) parsing controllato di date, prezzo e partecipanti
        LocalDateTime inizio;
        try {
            inizio = LocalDate.parse(dto.getDataInizioTxt())
                    .atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("⚠ Data di inizio non valida (usa il formato YYYY-MM-DD)");
        }

        LocalDateTime fine;
        try {
            fine = LocalDate.parse(dto.getDataFineTxt())
                    .atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("⚠ Data di fine non valida (usa il formato YYYY-MM-DD)");
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(dto.getPrezzoTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Prezzo non valido (deve essere un numero)");
        }

        int minPar;
        try {
            minPar = Integer.parseInt(dto.getMinPartecipantiTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Numero minimo di partecipanti non valido (deve essere un intero)");
        }

        // 2) validazioni di dominio
        ValidatoreVisitaInvito.validaDate(inizio, fine);
        ValidatoreVisitaInvito.validaLuogo(dto.getIndirizzo());
        ValidatoreVisitaInvito.validaPrezzo(prezzo);
        ValidatoreVisitaInvito.validaMinPartecipanti(minPar);
        ValidatoreVisitaInvito.validaDestinatari(dto.getDestinatari());

        // 3) costruzione del dominio già in stato PUBBLICATA
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

        // 4) persistenza
        if (!dao.save(vi)) {
            throw new RuntimeException("Errore durante il salvataggio della visita su invito");
        }

        // 5) notifica tutti gli osservatori
        notifier.notificaTutti(vi, "VISITA_INVITO_PUBBLICATA");
    }

    @Override
    public List<VisitaInvito> getVisiteCreateDa(String organizzatore) {
        return dao.findByOrganizzatore(organizzatore);
    }
}
