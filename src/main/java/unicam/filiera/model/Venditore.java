package unicam.filiera.model;

/**
 * Astrazione per utenti in grado di vendere.
 */
public abstract class Venditore extends UtenteAutenticato {
    protected Venditore(String username, String password,
                        String nome, String cognome, Ruolo ruolo) {
        super(username, password, nome, cognome, ruolo);
    }

    /** Azione di vendita, specifica dei sottotipi. */
    public void vendi() {
        System.out.println("[" + getRuolo() + "] può vendere prodotti.");
    }
}
