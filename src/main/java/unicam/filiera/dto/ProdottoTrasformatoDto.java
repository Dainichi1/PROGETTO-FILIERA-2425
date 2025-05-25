package unicam.filiera.dto;

import java.io.File;
import java.util.List;

/**
 * DTO per la creazione o modifica di un Prodotto Trasformato da parte del Trasformatore.
 * Incapsula tutti i campi del form della UI, compreso (per l’update) il nome originale,
 * e la lista delle fasi di produzione.
 */
public class ProdottoTrasformatoDto {

    private final String originalName;

    private final String nome;
    private final String descrizione;
    private final String quantitaTxt;
    private final String prezzoTxt;
    private final String indirizzo;
    private final List<File> certificati;
    private final List<File> foto;
    private final List<FaseProduzioneDto> fasiProduzione;

    // Costruttore per "nuovo prodotto trasformato" (originalName = null)
    public ProdottoTrasformatoDto(
            String nome,
            String descrizione,
            String quantitaTxt,
            String prezzoTxt,
            String indirizzo,
            List<File> certificati,
            List<File> foto,
            List<FaseProduzioneDto> fasiProduzione) {
        this(null, nome, descrizione, quantitaTxt, prezzoTxt, indirizzo, certificati, foto, fasiProduzione);
    }

    // Costruttore completo (usato per modifica)
    public ProdottoTrasformatoDto(
            String originalName,
            String nome,
            String descrizione,
            String quantitaTxt,
            String prezzoTxt,
            String indirizzo,
            List<File> certificati,
            List<File> foto,
            List<FaseProduzioneDto> fasiProduzione) {
        this.originalName = originalName;
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantitaTxt = quantitaTxt;
        this.prezzoTxt = prezzoTxt;
        this.indirizzo = indirizzo;
        this.certificati = certificati;
        this.foto = foto;
        this.fasiProduzione = fasiProduzione;
    }

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

    public List<FaseProduzioneDto> getFasiProduzione() {
        return fasiProduzione;
    }

    /**
     * Mini-DTO che rappresenta una singola fase di produzione
     * del prodotto trasformato. Serve per trasferire dal form UI
     * le info sulle fasi (descrizione, produttore, prodotto base).
     */
    public static class FaseProduzioneDto {
        private final String descrizioneFase;
        private final String produttore;         // può essere anche l'idProduttore
        private final String prodottoProduttore; // può essere anche l'idProdotto

        public FaseProduzioneDto(String descrizioneFase, String produttore, String prodottoProduttore) {
            this.descrizioneFase = descrizioneFase;
            this.produttore = produttore;
            this.prodottoProduttore = prodottoProduttore;
        }

        public String getDescrizioneFase() {
            return descrizioneFase;
        }

        public String getProduttore() {
            return produttore;
        }

        public String getProdottoProduttore() {
            return prodottoProduttore;
        }
    }
}
