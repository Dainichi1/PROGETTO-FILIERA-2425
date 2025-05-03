package unicam.filiera.service;

import unicam.filiera.dto.FieraDto;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.observer.FieraNotifier;
import unicam.filiera.dao.FieraDAO;
import unicam.filiera.dao.JdbcFieraDAO;
import unicam.filiera.util.ValidatoreFiera;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FieraServiceImpl implements FieraService {
    private final FieraDAO dao;
    private final FieraNotifier notifier;

    /** Iniezione di dipendenza (utile per i test) */
    public FieraServiceImpl(FieraDAO dao) {
        this.dao      = dao;
        this.notifier = FieraNotifier.getInstance();
    }

    /** Costruttore di convenienza per l’app reale */
    public FieraServiceImpl() {
        this(JdbcFieraDAO.getInstance());
    }

    @Override
    public void creaFiera(FieraDto dto, String organizzatore) {
        // 1) parsing controllato
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

        int minPartecipanti;
        try {
            minPartecipanti = Integer.parseInt(dto.getMinPartecipantiTxt());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("⚠ Numero minimo di partecipanti non valido (deve essere un intero)");
        }

        // 2) validazioni di dominio
        ValidatoreFiera.validaDate(inizio, fine);
        ValidatoreFiera.validaLuogo(dto.getIndirizzo());
        ValidatoreFiera.validaPrezzo(prezzo);
        ValidatoreFiera.validaMinPartecipanti(minPartecipanti);

        // 3) costruzione del dominio in stato PUBBLICATA
        Fiera f = new Fiera.Builder()
                .id(0)  // sovrascritto dal DB
                .dataInizio(inizio)
                .dataFine(fine)
                .prezzo(prezzo)
                .descrizione(dto.getDescrizione())
                .indirizzo(dto.getIndirizzo())
                .numeroMinPartecipanti(minPartecipanti)
                .stato(StatoEvento.PUBBLICATA)
                .organizzatore(organizzatore)
                .build();

        // 4) persistenza
        if (!dao.save(f)) {
            throw new RuntimeException("Errore durante il salvataggio della fiera");
        }

        // 5) notifica osservatori
        notifier.notificaTutti(f, "FIERA_PUBBLICATA");
    }

    @Override
    public List<Fiera> getFiereCreateDa(String organizzatore) {
        return dao.findByOrganizzatore(organizzatore);
    }

    @Override
    public List<Fiera> getFiereByStato(StatoEvento stato) {
        return dao.findByStato(stato);
    }
}
