package unicam.filiera.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Acquirente extends UtenteAutenticato {

    private double fondi;

    public Acquirente(String username, String password, String nome, String cognome, double fondi) {
        super(username, password, nome, cognome, Ruolo.ACQUIRENTE);
        this.fondi = fondi;
    }


    public void acquista() {
        System.out.println("[" + getRuolo() + "] può acquistare prodotti.");
    }
}
