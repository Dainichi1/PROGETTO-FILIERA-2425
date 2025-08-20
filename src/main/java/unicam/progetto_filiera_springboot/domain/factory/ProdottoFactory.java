package unicam.progetto_filiera_springboot.domain.factory;

import unicam.progetto_filiera_springboot.domain.model.Item;
import unicam.progetto_filiera_springboot.domain.model.Utente;

import java.math.BigDecimal;

public interface ProdottoFactory {
    Item creaProdotto(String nome,
                      String descrizione,
                      int quantita,
                      BigDecimal prezzo,
                      String indirizzo,
                      Utente creatoDa,
                      String certificatiCsv,
                      String fotoCsv);
}
