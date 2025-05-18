package unicam.filiera.util;

import unicam.filiera.model.Item;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;

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

    /**
     * Valida la quantità richiesta in base al tipo di Item.
     *
     * @param item      Item da validare (Prodotto o Pacchetto)
     * @param richiesta Quantità desiderata
     */
    public static void validaQuantitaItem(Item item, int richiesta) {
        if (item instanceof Prodotto p) {
            validaQuantita(richiesta, p.getQuantita());
        } else if (item instanceof Pacchetto pac) {
            validaQuantita(richiesta, pac.getQuantita());
        } else {
            throw new IllegalArgumentException("⚠ Tipo di item non supportato.");
        }
    }

    public static void validaFondi(double fondiDisponibili, double totaleAcquisto) {
        if (fondiDisponibili <= 0)
            throw new IllegalArgumentException("Non puoi procedere: saldo fondi a zero!");
        if (fondiDisponibili < totaleAcquisto)
            throw new IllegalArgumentException("Fondi insufficienti per completare l'acquisto.");
    }

}
