package unicam.filiera.model;

public class Animatore extends UtenteAutenticato {
    public Animatore(String username, String password,
                     String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.ANIMATORE);
    }

    public void organizzaEvento() {
        System.out.println("[" + getRuolo() + "] può organizzare eventi e visite guidate.");
    }
}
