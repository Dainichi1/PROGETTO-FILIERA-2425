package unicam.filiera.factory;

import unicam.filiera.model.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Factory per la creazione di Utente, UtenteAutenticato e sottotipi,
 * tramite uno schema di strategie aperto all'estensione.
 */
public final class UtenteFactory {
    private UtenteFactory() {
    }

    // DTO interno per incapsulare parametri di creazione
    private static record Data(String username, String password, String nome, String cognome, Ruolo ruolo, double fondi) {}


    // Registry di strategie per ciascun Ruolo
    private static final Map<Ruolo, Function<Data, Utente>> registry = new EnumMap<>(Ruolo.class);

    static {
        registry.put(Ruolo.PRODUTTORE, d -> new Produttore(d.username, d.password, d.nome, d.cognome));
        registry.put(Ruolo.TRASFORMATORE, d -> new Trasformatore(d.username, d.password, d.nome, d.cognome));
        registry.put(Ruolo.DISTRIBUTORE_TIPICITA,
                d -> new DistributoreTipicita(d.username, d.password, d.nome, d.cognome));
        registry.put(Ruolo.CURATORE, d -> new Curatore(d.username, d.password, d.nome, d.cognome));
        registry.put(Ruolo.ANIMATORE, d -> new Animatore(d.username, d.password, d.nome, d.cognome));
        registry.put(Ruolo.ACQUIRENTE,
                d -> new Acquirente(d.username, d.password, d.nome, d.cognome, d.fondi));

        registry.put(Ruolo.GESTORE_PIATTAFORMA,
                d -> new GestorePiattaforma(d.username, d.password, d.nome, d.cognome));
    }

    /**
     * Registra dinamicamente una nuova strategia di creazione per un Ruolo.
     */
    public static void register(Ruolo ruolo, Function<Data, Utente> creator) {
        registry.put(ruolo, creator);
    }

    /**
     * Restituisce un UtenteGenerico (non autenticato) per la navigazione del marketplace.
     */
    public static Utente creaUtenteGenerico() {
        return new UtenteGenerico();
    }

    /**
     * Crea l’utente per la registrazione in DB (utente autenticato base).
     */
    public static Utente creaUtenteRegistrazione(
            String username, String password,
            String nome, String cognome,
            Ruolo ruolo) {
        return new UtenteAutenticato(username, password, nome, cognome, ruolo);
    }

    /**
     * Crea l’attore appropriato dopo il login, tramite lookup nel registry.
     */
    public static Utente creaAttore(
            String username, String password,
            String nome, String cognome,
            Ruolo ruolo, double fondi) {
        Data d = new Data(username, password, nome, cognome, ruolo, fondi);
        return registry.getOrDefault(
                ruolo,
                data -> {
                    if (data.ruolo() == Ruolo.ACQUIRENTE)
                        return new Acquirente(data.username(), data.password(), data.nome(), data.cognome(), data.fondi());
                    else
                        return new UtenteAutenticato(data.username(), data.password(), data.nome(), data.cognome(), data.ruolo());
                }
        ).apply(d);

    }



    /**
     * Crea l’attore appropriato dopo il login, per ruoli diversi da ACQUIRENTE.
     */
    public static Utente creaAttore(
            String username, String password,
            String nome, String cognome,
            Ruolo ruolo) {
        return creaAttore(username, password, nome, cognome, ruolo, 0.0);
    }


}