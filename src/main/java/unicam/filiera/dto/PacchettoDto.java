package unicam.filiera.dto;

import java.io.File;
import java.util.List;

/**
 * DTO per trasferire i dati di un Pacchetto (nuovo o in modifica).
 */
public class PacchettoDto {
    /**
     * Nome originario del pacchetto prima della modifica; null se è un nuovo pacchetto
     */
    private final String originalName;

    private final String nome;
    private final String descrizione;
    private final String indirizzo;
    private final String prezzoTxt;
    private final String quantitaTxt;
    private final List<String> nomiProdotti;
    private final List<File> certificati;
    private final List<File> foto;

    /**
     * Costruttore per flusso “nuovo pacchetto”
     */
    public PacchettoDto(
            String nome,
            String descrizione,
            String indirizzo,
            String prezzoTxt,
            String quantitaTxt,
            List<String> nomiProdotti,
            List<File> certificati,
            List<File> foto) {
        this(null, nome, descrizione, indirizzo, prezzoTxt, quantitaTxt, nomiProdotti, certificati, foto);
    }

    /**
     * Costruttore completo, usato per il flusso di modifica
     */
    public PacchettoDto(
            String originalName,
            String nome,
            String descrizione,
            String indirizzo,
            String prezzoTxt,
            String quantitaTxt,
            List<String> nomiProdotti,
            List<File> certificati,
            List<File> foto) {
        this.originalName = originalName;
        this.nome = nome;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.prezzoTxt = prezzoTxt;
        this.quantitaTxt = quantitaTxt;
        this.nomiProdotti = List.copyOf(nomiProdotti);
        this.certificati = List.copyOf(certificati);
        this.foto = List.copyOf(foto);
    }

    /**
     * @return il nome originario in caso di modifica, altrimenti null
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

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getPrezzoTxt() {
        return prezzoTxt;
    }

    public String getQuantitaTxt() {
        return quantitaTxt;
    }

    public List<String> getNomiProdotti() {
        return nomiProdotti;
    }

    public List<File> getCertificati() {
        return certificati;
    }

    public List<File> getFoto() {
        return foto;
    }
}
