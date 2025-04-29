package unicam.filiera.model;

import java.time.LocalDateTime;

/**
 * Template Method + Builder: super-classe di Fiera e (in futuro) VisitaSuInvito.
 */
public abstract class Evento {

    /* ============== ATTRIBUTI COMUNI ============== */
    private final String          titolo;
    private final String          descrizione;
    private final LocalDateTime   dataInizio;
    private final LocalDateTime   dataFine;
    private final double          prezzoBiglietto;
    private final String          indirizzo;
    private final String          creatoDa;
    private       StatoEvento     stato;

    /* ===== costruttore PROTETTO, invocato dal Builder delle sottoclassi ===== */
    protected Evento(Builder<?> b) {
        this.titolo          = b.titolo;
        this.descrizione     = b.descrizione;
        this.dataInizio      = b.dataInizio;
        this.dataFine        = b.dataFine;
        this.prezzoBiglietto = b.prezzoBiglietto;
        this.indirizzo       = b.indirizzo;
        this.creatoDa        = b.creatoDa;
        this.stato           = b.stato;
    }

    /* ---------------- TEMPLATE METHOD pubbica() ---------------- */
    public final void pubblica() {
        if (dataInizio.isAfter(dataFine))
            throw new IllegalStateException("Data inizio successiva alla data fine");

        if (!isLuogoDisponibile())
            throw new IllegalStateException("Indirizzo occupato in quelle date");

        doPubblica();          // hook specifico
        stato = StatoEvento.ATTIVA;
    }

    /* ---- hook overridabile ---- */
    protected abstract void doPubblica();

    /* ---- logica intercambiabile (Strategy) ---- */
    protected boolean isLuogoDisponibile() {
        // qui potresti delegare ad un DAO o ad una Strategy
        return true;
    }

    /* ============== GETTER ============== */
    public String         getTitolo()          { return titolo; }
    public String         getDescrizione()     { return descrizione; }
    public LocalDateTime  getDataInizio()      { return dataInizio; }
    public LocalDateTime  getDataFine()        { return dataFine; }
    public double         getPrezzoBiglietto() { return prezzoBiglietto; }
    public String         getIndirizzo()       { return indirizzo; }
    public String         getCreatoDa()        { return creatoDa; }
    public StatoEvento    getStato()           { return stato; }

    /* ===========================================================
       BUILDER generico (self-bounded generic)
       =========================================================== */
    public static abstract class Builder<B extends Builder<B>> {
        private String          titolo, descrizione, indirizzo, creatoDa;
        private LocalDateTime   dataInizio, dataFine;
        private double          prezzoBiglietto;
        private StatoEvento     stato = StatoEvento.IN_PREPARAZIONE;

        /* ---------- fluent setters ---------- */
        public B titolo(String v)          { this.titolo=v; return self(); }
        public B descrizione(String v)     { this.descrizione=v; return self(); }
        public B dataInizio(LocalDateTime v){this.dataInizio=v;return self();}
        public B dataFine(LocalDateTime v) { this.dataFine=v; return self(); }
        public B prezzoBiglietto(double v) { this.prezzoBiglietto=v;return self();}
        public B indirizzo(String v)       { this.indirizzo=v; return self(); }
        public B creatoDa(String v)        { this.creatoDa=v; return self(); }
        public B stato(StatoEvento v)      { this.stato=v; return self(); }

        /* restituisce this castato correttamente */
        protected abstract B self();
    }
}
