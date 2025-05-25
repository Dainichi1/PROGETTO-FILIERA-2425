package unicam.filiera.controller;

import unicam.filiera.model.observer.OsservatoreProdottoTrasformato;
import unicam.filiera.model.observer.ProdottoTrasformatoNotifier;

public class ObserverManagerProdottoTrasformato {

    public static void registraOsservatore(OsservatoreProdottoTrasformato o) {
        ProdottoTrasformatoNotifier.getInstance().registraOsservatore(o);
    }

    public static void rimuoviOsservatore(OsservatoreProdottoTrasformato o) {
        ProdottoTrasformatoNotifier.getInstance().rimuoviOsservatore(o);
    }
}
