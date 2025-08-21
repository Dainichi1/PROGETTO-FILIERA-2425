package unicam.progetto_filiera_springboot.domain.factory;

import org.springframework.stereotype.Component;
import unicam.progetto_filiera_springboot.domain.model.Item;
import unicam.progetto_filiera_springboot.domain.model.Pacchetto;
import unicam.progetto_filiera_springboot.domain.model.Utente;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class DefaultPacchettoFactory implements PacchettoFactory {

    @Override
    public Item creaPacchetto(String nome,
                              String descrizione,
                              int quantita,
                              BigDecimal prezzoTotale,
                              String indirizzo,
                              Utente creatoDa,
                              String certificatiCsv,
                              String fotoCsv) {

        String nomeOk = trimOrNull(nome);
        String descOk = trimOrNull(descrizione);
        String indOk  = trimOrNull(indirizzo);
        String certOk = emptyToNull(certificatiCsv);
        String fotoOk = emptyToNull(fotoCsv);

        Pacchetto p = new Pacchetto(
                nomeOk,
                descOk,
                quantita,
                Objects.requireNonNull(prezzoTotale, "prezzoTotale è obbligatorio"),
                indOk,
                Objects.requireNonNull(creatoDa, "creatoDa è obbligatorio")
        );
        p.setCertificati(certOk);
        p.setFoto(fotoOk);
        return p;
    }

    private String trimOrNull(String s) { return s == null ? null : s.trim(); }

    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
