package unicam.filiera.model;

public class Venditore extends UtenteAutenticato {

    public Venditore(String username, String password, String nome, String cognome, Ruolo ruolo) {
        super(username, password, nome, cognome, ruolo);
    }

    public void vendi() {
        System.out.println("[" + getRuolo() + "] può vendere prodotti.");
    }
}
