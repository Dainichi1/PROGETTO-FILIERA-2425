package unicam.filiera.util;

public class ValidatoreMarketplace {

    /** Controlla che la quantità sia positiva */
    public static void validaQuantita(int quantita) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("⚠ La quantità deve essere maggiore di zero");
        }
    }

    /** Controlla che il tipo sia corretto */
    public static void validaTipo(String tipo) {
        if (!tipo.equals("Prodotto") && !tipo.equals("Pacchetto")) {
            throw new IllegalArgumentException("⚠ Tipo non valido: deve essere 'Prodotto' o 'Pacchetto'");
        }
    }

    /** Controlla che il nome non sia vuoto */
    public static void validaNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("⚠ Nome mancante o vuoto");
        }
    }

    /** Controlla che la quantità sia valida prima di aggiungere al carrello */
    public static void validaQuantitaAggiunta(int richiesta, Object disponibileObj) {
        if (richiesta <= 0) {
            throw new IllegalArgumentException("⚠ Devi selezionare almeno una quantità maggiore di 0.");
        }
        if (disponibileObj instanceof Integer disponibile) {
            if (richiesta > disponibile) {
                throw new IllegalArgumentException("⚠ Quantità richiesta superiore alla disponibilità.");
            }
        }
    }

}
