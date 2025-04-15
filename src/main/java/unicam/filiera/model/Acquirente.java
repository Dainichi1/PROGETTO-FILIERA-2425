package unicam.filiera.model;

public class Acquirente extends UtenteAutenticato {

    public Acquirente(String username, String password, String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.ACQUIRENTE);
    }

    public void acquista() {
        System.out.println("[" + getRuolo() + "] può acquistare prodotti.");
    }
}
