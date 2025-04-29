package unicam.filiera.model;

public class GestorePiattaforma extends UtenteAutenticato {
    public GestorePiattaforma(String username, String password,
                              String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.GESTORE_PIATTAFORMA);
    }

    public void gestisciPiattaforma() {
        System.out.println("[" + getRuolo() + "] può gestire autorizzazioni e aspetti amministrativi.");
    }
}
