package unicam.filiera.model;

import lombok.Getter;
import lombok.Setter;
import unicam.filiera.dto.EventoTipo;

import java.time.LocalDate;
import java.util.List;

/**
 * Classe astratta base per tutti gli eventi (VisitaInvito, Fiera, ...).
 * Raccoglie i campi comuni per evitare duplicazioni.
 */
@Getter
public abstract class Evento {

    private final String nome;
    private final String descrizione;
    private final String indirizzo;
    private final LocalDate dataInizio;
    private final LocalDate dataFine;
    private final String creatoDa;

    private final EventoTipo tipo;

    @Setter
    private StatoEvento stato;

    protected Evento(String nome,
                     String descrizione,
                     String indirizzo,
                     LocalDate dataInizio,
                     LocalDate dataFine,
                     String creatoDa,
                     StatoEvento stato,
                     EventoTipo tipo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.creatoDa = creatoDa;
        this.stato = stato;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return String.format(
                "Evento[nome=%s, descr=%s, indirizzo=%s, dal=%s al=%s, stato=%s, tipo=%s]",
                nome, descrizione, indirizzo,
                dataInizio, dataFine,
                stato != null ? stato.name() : "N/D",
                tipo != null ? tipo.name() : "N/D"
        );
    }
}
