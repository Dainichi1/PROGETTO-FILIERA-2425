package unicam.filiera.model;

public class Trasformatore extends Venditore {
    public Trasformatore(String username, String password,
                         String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.TRASFORMATORE);
    }

    public void trasformaProdotto() {
        System.out.println("[" + getRuolo() + "] può trasformare prodotti.");
    }
}
