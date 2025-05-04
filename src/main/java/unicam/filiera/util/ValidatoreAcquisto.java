package unicam.filiera.util;

public class ValidatoreAcquisto {

    public static void validaQuantita(int richiesta, int disponibile) {
        if (richiesta <= 0)
            throw new IllegalArgumentException("⚠ La quantità richiesta deve essere maggiore di 0.");
        if (richiesta > disponibile)
            throw new IllegalArgumentException("⚠ Quantità richiesta superiore alla disponibilità.");
    }

    public static void validaMetodoPagamento(Object metodo) {
        if (metodo == null)
            throw new IllegalArgumentException("⚠ Metodo di pagamento non selezionato.");
    }
}
