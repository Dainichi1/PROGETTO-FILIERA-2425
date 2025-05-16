package unicam.filiera.dto;

import java.io.File;
import java.util.List;

/**
 * DTO per la creazione o modifica di un Prodotto da parte del produttore.
 * Incapsula tutti i campi del form della UI, compreso (per l’update) il nome originale.
 */
public class ProdottoDto {
    /**
     * Nel flusso di modifica, contiene il nome con cui il prodotto
     * era stato salvato originariamente (prima della correzione);
     * altrimenti null.
     */
    private final String originalName;

    private final String nome;
    private final String descrizione;
    private final String quantitaTxt;
    private final String prezzoTxt;
    private final String indirizzo;
    private final List<File> certificati;
    private final List<File> foto;

    /**
     * Costruttore per il flusso “nuovo prodotto” (originalName = null).
     */
    public ProdottoDto(
            String nome,
            String descrizione,
            String quantitaTxt,
            String prezzoTxt,
            String indirizzo,
            List<File> certificati,
            List<File> foto) {
        this(null, nome, descrizione, quantitaTxt, prezzoTxt, indirizzo, certificati, foto);
    }

    /**
     * Costruttore completo, usato soprattutto per la modifica:
     * originalName = nome del prodotto prima della modifica.
     */
    public ProdottoDto(
            String originalName,
            String nome,
            String descrizione,
            String quantitaTxt,
            String prezzoTxt,
            String indirizzo,
            List<File> certificati,
            List<File> foto) {
        this.originalName = originalName;
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantitaTxt = quantitaTxt;
        this.prezzoTxt = prezzoTxt;
        this.indirizzo = indirizzo;
        this.certificati = certificati;
        this.foto = foto;
    }

    /**
     * @return il nome originario, se in modalità modifica; altrimenti null
     */
    public String getOriginalName() {
        return originalName;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getQuantitaTxt() {
        return quantitaTxt;
    }

    public String getPrezzoTxt() {
        return prezzoTxt;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public List<File> getCertificati() {
        return certificati;
    }

    public List<File> getFoto() {
        return foto;
    }
}
