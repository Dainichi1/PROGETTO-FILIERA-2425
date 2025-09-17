package unicam.filiera.model;

import lombok.Getter;
import lombok.Setter;
import unicam.filiera.dto.ItemTipo;

import java.util.List;

/**
 * Super-classe astratta per gli elementi commercializzabili (Prodotto, Pacchetto, …).
 * Raccoglie i campi e la logica condivisa, così da evitare duplicazioni.
 */
@Getter
public abstract class Item {

    private final String nome;
    private final String descrizione;
    private final String indirizzo;
    private final List<String> certificati;
    private final List<String> foto;
    private final String creatoDa;

    private final ItemTipo tipo;

    @Setter
    private StatoProdotto stato;

    @Setter
    private String commento;

    protected Item(String nome,
                   String descrizione,
                   String indirizzo,
                   List<String> certificati,
                   List<String> foto,
                   String creatoDa,
                   StatoProdotto stato,
                   String commento,
                   ItemTipo tipo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.certificati = certificati;
        this.foto = foto;
        this.creatoDa = creatoDa;
        this.stato = stato;
        this.commento = commento;
        this.tipo = tipo;
    }

    public abstract Long getId();
    public abstract int getQuantita();
    public abstract double getPrezzo();

    @Override
    public String toString() {
        return String.format(
                "Item[nome=%s, descr=%s, indirizzo=%s, tipo=%s, stato=%s]",
                nome,
                descrizione,
                indirizzo,
                tipo != null ? tipo.name() : "N/D",
                stato != null ? stato.name() : "N/D"
        );
    }
}
