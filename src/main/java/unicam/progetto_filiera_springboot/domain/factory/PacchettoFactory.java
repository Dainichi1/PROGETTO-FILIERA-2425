package unicam.progetto_filiera_springboot.domain.factory;

import unicam.progetto_filiera_springboot.domain.model.Item;
import unicam.progetto_filiera_springboot.domain.model.Utente;

import java.math.BigDecimal;

public interface PacchettoFactory {

    Item creaPacchetto(String nome,
                       String descrizione,
                       int quantita,
                       BigDecimal prezzoTotale,
                       String indirizzo,
                       Utente creatoDa,
                       String certificatiCsv,
                       String fotoCsv);

    default Item creaPacchetto(String nome,
                               String descrizione,
                               int quantita,
                               BigDecimal prezzoTotale,
                               String indirizzo,
                               Utente creatoDa) {
        return creaPacchetto(nome, descrizione, quantita, prezzoTotale, indirizzo, creatoDa, null, null);
    }
}
