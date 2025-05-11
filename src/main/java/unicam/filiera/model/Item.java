package unicam.filiera.model;

import java.util.List;

/**
 * Super‑classe astratta per gli elementi commercializzabili (Prodotto, Pacchetto, …).
 * Raccoglie i campi e la logica condivisa, così da evitare duplicazioni.
 */
public abstract class Item {

    private final String nome;
    private final String descrizione;
    private final String indirizzo;
    private final List<String> certificati;
    private final List<String> foto;
    private final String creatoDa;

    private StatoProdotto stato;
    private String commento;

    /**
     * Costruttore protetto: deve essere invocato dalle sottoclassi
     * (es. Prodotto, Pacchetto) nel proprio builder.
     */
    protected Item(String nome,
                   String descrizione,
                   String indirizzo,
                   List<String> certificati,
                   List<String> foto,
                   String creatoDa,
                   StatoProdotto stato,
                   String commento) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.certificati = certificati;
        this.foto = foto;
        this.creatoDa = creatoDa;
        this.stato = stato;
        this.commento = commento;
    }

    // ---- Getter ----
    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public List<String> getCertificati() {
        return certificati;
    }

    public List<String> getFoto() {
        return foto;
    }

    public String getCreatoDa() {
        return creatoDa;
    }

    public StatoProdotto getStato() {
        return stato;
    }

    public String getCommento() {
        return commento;
    }

    /**
     * Permette al Curatore di aggiornare lo stato.
     */
    public void setStato(StatoProdotto stato) {
        this.stato = stato;
    }

    /**
     * Permette al Curatore di aggiungere un commento di rifiuto o approvazione.
     */
    public void setCommento(String commento) {
        this.commento = commento;
    }

    @Override
    public String toString() {
        return String.format(
                "Item[nome=%s, descr=%s, indirizzo=%s, stato=%s]",
                nome,
                descrizione,
                indirizzo,
                stato != null ? stato.name() : "N/D"
        );
    }
}
