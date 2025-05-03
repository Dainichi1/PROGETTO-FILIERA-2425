package unicam.filiera.model;

import java.time.LocalDateTime;
import java.util.List;

public class VisitaInvito extends Evento {
    private final String organizzatore;
    private final int numeroMinPartecipanti;
    private final List<String> destinatari;

    private VisitaInvito(Builder b) {
        super(
                b.id,
                b.dataInizio,
                b.dataFine,
                b.prezzo,
                b.descrizione,
                b.indirizzo,
                b.stato
        );
        this.organizzatore          = b.organizzatore;
        this.numeroMinPartecipanti  = b.numeroMinPartecipanti;
        this.destinatari            = List.copyOf(b.destinatari);
    }

    public String getOrganizzatore()                { return organizzatore; }
    public int    getNumeroMinPartecipanti()        { return numeroMinPartecipanti; }
    public List<String> getDestinatari()            { return destinatari; }

    @Override
    public String toString() {
        return String.format(
                "VisitaInvito[id=%d, %s→%s, prezzo=%.2f, minPartec=%d, org=%s, dest=%s]",
                getId(), getDataInizio(), getDataFine(),
                getPrezzo(), numeroMinPartecipanti,
                organizzatore, String.join(",", destinatari)
        );
    }

    public static class Builder {
        private long              id;
        private LocalDateTime     dataInizio;
        private LocalDateTime     dataFine;
        private double            prezzo;
        private String            descrizione;
        private String            indirizzo;
        private String            organizzatore;
        private int               numeroMinPartecipanti;
        private List<String>      destinatari;
        private StatoEvento       stato = StatoEvento.IN_PREPARAZIONE;

        public Builder id(long id)                            { this.id = id; return this; }
        public Builder dataInizio(LocalDateTime dt)          { this.dataInizio = dt; return this; }
        public Builder dataFine(LocalDateTime dt)            { this.dataFine = dt; return this; }
        public Builder prezzo(double p)                      { this.prezzo = p; return this; }
        public Builder descrizione(String d)                 { this.descrizione = d; return this; }
        public Builder indirizzo(String i)                   { this.indirizzo = i; return this; }
        public Builder organizzatore(String o)               { this.organizzatore = o; return this; }
        public Builder numeroMinPartecipanti(int n)          { this.numeroMinPartecipanti = n; return this; }
        public Builder destinatari(List<String> dest)        { this.destinatari = dest; return this; }
        public Builder stato(StatoEvento s)                  { this.stato = s; return this; }

        public VisitaInvito build() {
            if (dataInizio==null||dataFine==null||descrizione==null
                    ||indirizzo==null||organizzatore==null||destinatari==null)
                throw new IllegalStateException("Campi obbligatori mancanti");
            return new VisitaInvito(this);
        }
    }
}
