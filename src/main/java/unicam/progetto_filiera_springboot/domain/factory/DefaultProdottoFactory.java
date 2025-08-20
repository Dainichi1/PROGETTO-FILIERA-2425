package unicam.progetto_filiera_springboot.domain.factory;

import org.springframework.stereotype.Component;
import unicam.progetto_filiera_springboot.domain.model.Item;
import unicam.progetto_filiera_springboot.domain.model.Prodotto;
import unicam.progetto_filiera_springboot.domain.model.Utente;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class DefaultProdottoFactory implements ProdottoFactory {

    @Override
    public Item creaProdotto(String nome,
                             String descrizione,
                             int quantita,
                             BigDecimal prezzo,
                             String indirizzo,
                             Utente creatoDa,
                             String certificatiCsv,
                             String fotoCsv) {

        // (Opzionale) normalizzazioni leggere per evitare NPE/whitespace
        String nomeOk = trimOrNull(nome);
        String descrOk = trimOrNull(descrizione);
        String indirOk = trimOrNull(indirizzo);
        String certOk = emptyToNull(certificatiCsv);
        String fotoOk = emptyToNull(fotoCsv);

        Prodotto p = new Prodotto(nomeOk, descrOk, quantita, prezzo, indirOk, Objects.requireNonNull(creatoDa));
        p.setCertificati(certOk);
        p.setFoto(fotoOk);
        return p; // ritorna come Item
    }

    private String trimOrNull(String s) { return s == null ? null : s.trim(); }
    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
