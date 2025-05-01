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
import java.util.List;

/**
 * Implementazione JDBC del FieraService.
 */
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
        // 1) validazione dei campi
        LocalDateTime inizio = LocalDate
                .parse(dto.getDataInizioTxt()).atStartOfDay();
        LocalDateTime fine   = LocalDate
                .parse(dto.getDataFineTxt()).atStartOfDay();
        double prezzo         = Double.parseDouble(dto.getPrezzoTxt());
        int minPartecipanti   = Integer.parseInt(dto.getMinPartecipantiTxt());

        ValidatoreFiera.validaDate(inizio, fine);
        ValidatoreFiera.validaLuogo(dto.getIndirizzo());
        ValidatoreFiera.validaPrezzo(prezzo);
        ValidatoreFiera.validaMinPartecipanti(minPartecipanti);

        // 2) costruzione del dominio con stato PUBBLICATA
        Fiera f = new Fiera.Builder()
                .id(0) // verrà sovrascritto dal DB
                .dataInizio(inizio)
                .dataFine(fine)
                .prezzo(prezzo)
                .descrizione(dto.getDescrizione())
                .indirizzo(dto.getIndirizzo())
                .numeroMinPartecipanti(minPartecipanti)
                .stato(StatoEvento.PUBBLICATA)   // qui
                .organizzatore(organizzatore)
                .build();

        // 3) persistenza
        if (!dao.save(f)) {
            throw new RuntimeException("Errore durante il salvataggio della fiera");
        }

        // 4) notifica gli osservatori che la fiera è stata pubblicata
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
