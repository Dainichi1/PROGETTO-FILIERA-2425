package unicam.filiera_agricola_2425.dtos;

import lombok.Data;
import unicam.filiera_agricola_2425.models.Prodotto;
import unicam.filiera_agricola_2425.models.Produttore;

@Data
public class ProdottoForm {
    private String nome;
    private double prezzo;
    private int quantita;
    private String descrizione;
    private String certificazione;
    private String immagine;

    public Prodotto toProdotto(Produttore produttore) {
        Prodotto p = new Prodotto();
        p.setNome(nome);
        p.setPrezzo(prezzo);
        p.setQuantita(quantita);
        p.setDescrizione(descrizione);
        p.setCertificazione(certificazione);
        p.setImmagine(immagine);
        p.setProduttore(produttore);
        return p;
    }
}
