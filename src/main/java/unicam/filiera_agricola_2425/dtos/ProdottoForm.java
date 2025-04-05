package unicam.filiera_agricola_2425.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera_agricola_2425.models.Prodotto;
import unicam.filiera_agricola_2425.models.Produttore;

import java.util.List;

@Data
public class ProdottoForm {
    private String nome;
    private double prezzo;
    private int quantita;
    private String descrizione;

    private List<MultipartFile> immagini;
    private List<MultipartFile> certificati;

    public Prodotto toProdotto(Produttore produttore) {
        Prodotto p = new Prodotto();
        p.setNome(nome);
        p.setPrezzo(prezzo);
        p.setQuantita(quantita);
        p.setDescrizione(descrizione);
        p.setProduttore(produttore);
        return p;
    }
}
