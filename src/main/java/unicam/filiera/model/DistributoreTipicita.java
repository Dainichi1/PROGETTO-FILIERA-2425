package unicam.filiera.model;

public class DistributoreTipicita extends Venditore {
    public DistributoreTipicita(String username, String password,
                                String nome, String cognome) {
        super(username, password, nome, cognome, Ruolo.DISTRIBUTORE_TIPICITA);
    }

    public void creaPacchetto() {
        System.out.println("[" + getRuolo() + "] può creare pacchetti di prodotti.");
    }
}