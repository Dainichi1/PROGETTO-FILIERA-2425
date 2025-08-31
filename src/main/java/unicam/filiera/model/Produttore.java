package unicam.filiera.model;

public class Produttore extends Venditore {

    public Produttore(String username, String password,
                      String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.PRODUTTORE);
    }

    public void creaProdotto() {
        System.out.println("[" + getRuolo() + "] può creare nuovi prodotti.");
    }
}
