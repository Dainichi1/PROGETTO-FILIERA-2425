package unicam.filiera.model;


public class Curatore extends UtenteAutenticato {
    public Curatore(String username, String password,
                    String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.CURATORE);
    }

    public void validaContenuti() {
        System.out.println("[" + getRuolo() + "] può validare contenuti e approvarli.");
    }
}
