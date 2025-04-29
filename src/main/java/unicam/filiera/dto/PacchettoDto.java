package unicam.filiera.dto;

import java.io.File;
import java.util.List;

/**
 * DTO per trasferire i dati di un nuovo Pacchetto dalla View al Service.
 */
public class PacchettoDto {
    private final String nome;
    private final String descrizione;
    private final String indirizzo;
    private final String prezzoTxt;
    private final List<String> nomiProdotti;
    private final List<File> certificati;
    private final List<File> foto;

    public PacchettoDto(
            String nome,
            String descrizione,
            String indirizzo,
            String prezzoTxt,
            List<String> nomiProdotti,
            List<File> certificati,
            List<File> foto) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.indirizzo = indirizzo;
        this.prezzoTxt = prezzoTxt;
        this.nomiProdotti = List.copyOf(nomiProdotti);
        this.certificati = List.copyOf(certificati);
        this.foto = List.copyOf(foto);
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

    /**
     * Restituisce i nomi (o identificativi) dei prodotti inclusi nel pacchetto.
     */
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
