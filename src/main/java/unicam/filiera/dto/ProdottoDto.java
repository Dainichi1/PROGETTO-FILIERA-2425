package unicam.filiera.dto;

import java.io.File;
import java.util.List;

/**
 * DTO per la creazione di un nuovo Prodotto da parte del produttore.
 * Incapsula tutti i campi del form della UI.
 */
public class ProdottoDto {
    private final String nome;
    private final String descrizione;
    private final String quantitaTxt;
    private final String prezzoTxt;
    private final String indirizzo;
    private final List<File> certificati;
    private final List<File> foto;

    public ProdottoDto(
            String nome,
            String descrizione,
            String quantitaTxt,
            String prezzoTxt,
            String indirizzo,
            List<File> certificati,
            List<File> foto) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantitaTxt = quantitaTxt;
        this.prezzoTxt = prezzoTxt;
        this.indirizzo = indirizzo;
        this.certificati = certificati;
        this.foto = foto;
    }

    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public String getQuantitaTxt() { return quantitaTxt; }
    public String getPrezzoTxt() { return prezzoTxt; }
    public String getIndirizzo() { return indirizzo; }
    public List<File> getCertificati() { return certificati; }
    public List<File> getFoto() { return foto; }
}

