package unicam.filiera_agricola_2425.factories;

import unicam.filiera_agricola_2425.models.Ruolo;

import java.util.HashMap;
import java.util.Map;

public class UtenteFactoryProvider {
    private static final Map<Ruolo, UtenteFactory> factoryMap = new HashMap<>();

    static {
        factoryMap.put(Ruolo.PRODUTTORE, new ProduttoreFactory());
        factoryMap.put(Ruolo.CURATORE, new CuratoreFactory());

    }

    public static UtenteFactory getFactory(Ruolo ruolo) {
        return factoryMap.get(ruolo);
    }
}
