package unicam.filiera.model;

import lombok.Getter;
import unicam.filiera.dto.EventoTipo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Evento specifico: Visita su Invito.
 * Include i campi aggiuntivi rispetto a {@link Evento}.
 */
@Getter
public class VisitaInvito extends Evento {

    private final Long id;
    private final List<String> destinatari;

    private VisitaInvito(Builder b) {
        super(
                b.nome,
                b.descrizione,
                b.indirizzo,
                b.dataInizio,
                b.dataFine,
                b.creatoDa,
                b.stato,
                EventoTipo.VISITA
        );
        this.id = b.id;
        this.destinatari = b.destinatari == null
                ? List.of()
                : Collections.unmodifiableList(List.copyOf(b.destinatari));
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

        private List<String> destinatari;

        public Builder id(Long i) { this.id = i; return this; }
        public Builder nome(String n) { this.nome = n; return this; }
        public Builder descrizione(String d) { this.descrizione = d; return this; }
        public Builder indirizzo(String i) { this.indirizzo = i; return this; }
        public Builder dataInizio(LocalDate di) { this.dataInizio = di; return this; }
        public Builder dataFine(LocalDate df) { this.dataFine = df; return this; }
        public Builder creatoDa(String u) { this.creatoDa = u; return this; }
        public Builder stato(StatoEvento s) { this.stato = s; return this; }
        public Builder destinatari(List<String> d) { this.destinatari = d; return this; }

        public VisitaInvito build() {
            if (nome == null || descrizione == null || indirizzo == null
                    || dataInizio == null || dataFine == null
                    || creatoDa == null || stato == null) {
                throw new IllegalStateException("⚠ Campi obbligatori mancanti");
            }
            if (dataFine.isBefore(dataInizio)) {
                throw new IllegalStateException("⚠ La data di fine deve essere successiva a quella di inizio");
            }
            if (destinatari == null || destinatari.isEmpty()) {
                throw new IllegalStateException("⚠ Devi specificare almeno un destinatario");
            }
            return new VisitaInvito(this);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " → destinatari=" + destinatari;
    }
}
