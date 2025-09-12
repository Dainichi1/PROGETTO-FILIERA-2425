package unicam.filiera.model;

import lombok.Getter;
import unicam.filiera.dto.EventoTipo;

import java.time.LocalDate;

/**
 * Evento specifico: Fiera.
 * È pubblica e visibile a tutti gli ACQUIRENTE.
 * A differenza di {@link VisitaInvito} non ha destinatari,
 * ma ha un campo aggiuntivo: prezzo di ingresso.
 */
@Getter
public class Fiera extends Evento {

    private final Long id;
    private final double prezzo;

    private Fiera(Builder b) {
        super(
                b.nome,
                b.descrizione,
                b.indirizzo,
                b.dataInizio,
                b.dataFine,
                b.creatoDa,
                b.stato,
                EventoTipo.FIERA
        );
        this.id = b.id;
        this.prezzo = b.prezzo;
    }

    /* ================== BUILDER ================== */
    public static class Builder {
        private Long id;
        private String nome;
        private String descrizione;
        private String indirizzo;
        private LocalDate dataInizio;
        private LocalDate dataFine;
        private String creatoDa;
        private StatoEvento stato;

        private double prezzo;

        public Builder id(Long i) { this.id = i; return this; }
        public Builder nome(String n) { this.nome = n; return this; }
        public Builder descrizione(String d) { this.descrizione = d; return this; }
        public Builder indirizzo(String i) { this.indirizzo = i; return this; }
        public Builder dataInizio(LocalDate di) { this.dataInizio = di; return this; }
        public Builder dataFine(LocalDate df) { this.dataFine = df; return this; }
        public Builder creatoDa(String u) { this.creatoDa = u; return this; }
        public Builder stato(StatoEvento s) { this.stato = s; return this; }
        public Builder prezzo(double p) { this.prezzo = p; return this; }

        public Fiera build() {
            if (nome == null || descrizione == null || indirizzo == null
                    || dataInizio == null || dataFine == null
                    || creatoDa == null || stato == null) {
                throw new IllegalStateException("⚠ Campi obbligatori mancanti");
            }
            if (dataFine.isBefore(dataInizio)) {
                throw new IllegalStateException("⚠ La data di fine deve essere successiva a quella di inizio");
            }
            if (prezzo < 0) {
                throw new IllegalStateException("⚠ Il prezzo non può essere negativo");
            }
            return new Fiera(this);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " → prezzo=" + prezzo;
    }
}
