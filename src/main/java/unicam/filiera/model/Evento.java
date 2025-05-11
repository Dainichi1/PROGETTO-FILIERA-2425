// -------- Evento.java --------
package unicam.filiera.model;

import java.time.LocalDateTime;

/**
 * Super‐classe astratta per tutti gli eventi (fiere, visite su invito, …).
 */
public abstract class Evento {
    private long id;
    private final LocalDateTime dataInizio;
    private final LocalDateTime dataFine;
    private final double prezzo;
    private final String descrizione;
    private final String indirizzo;
    private StatoEvento stato;

    protected Evento(long id,
                     LocalDateTime dataInizio,
                     LocalDateTime dataFine,
                     double prezzo,
                     String descrizione,
                     String indirizzo,
                     StatoEvento stato) {
        this.id = id;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.stato = stato;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDataInizio() {
        return dataInizio;
    }

    public LocalDateTime getDataFine() {
        return dataFine;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public StatoEvento getStato() {
        return stato;
    }

    public void setStato(StatoEvento s) {
        this.stato = s;
    }

    @Override
    public String toString() {
        return String.format(
                "Evento[id=%d, %s → %s, prezzo=%.2f, stato=%s, descr=%s, addr=%s]",
                id, dataInizio, dataFine, prezzo, stato, descrizione, indirizzo
        );
    }
}
