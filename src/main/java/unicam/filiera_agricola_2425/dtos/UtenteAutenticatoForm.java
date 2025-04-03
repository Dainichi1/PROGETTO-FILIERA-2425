package unicam.filiera_agricola_2425.dtos;

import lombok.Data;
import unicam.filiera_agricola_2425.models.*;

@Data
public class UtenteAutenticatoForm {
    private String nome;
    private String cognome;
    private String username;
    private String password;
    private Ruolo ruolo;

    public UtenteAutenticato toUtente() {
        return switch (ruolo) {
            case ACQUIRENTE -> {
                Acquirente a = new Acquirente();
                populate(a);
                yield a;
            }
            case CURATORE -> {
                Curatore c = new Curatore();
                populate(c);
                yield c;
            }
            case ANIMATORE -> {
                Animatore a = new Animatore();
                populate(a);
                yield a;
            }
            case GESTORE -> {
                GestorePiattaforma g = new GestorePiattaforma();
                populate(g);
                yield g;
            }
            case PRODUTTORE -> {
                Produttore p = new Produttore();
                populate(p);
                yield p;
            }
            case TRASFORMATORE -> {
                Trasformatore t = new Trasformatore();
                populate(t);
                yield t;
            }
            case DISTRIBUTORE -> {
                DistributoreTipicita d = new DistributoreTipicita();
                populate(d);
                yield d;
            }
        };
    }

    private void populate(UtenteAutenticato utente) {
        utente.setNome(this.nome);
        utente.setCognome(this.cognome);
        utente.setUsername(this.username);
        utente.setPassword(this.password);
        utente.setRuolo(this.ruolo);
    }
}
