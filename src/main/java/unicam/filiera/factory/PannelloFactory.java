package unicam.filiera.factory;

import unicam.filiera.controller.AutenticazioneController;
import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.view.*;

import javax.swing.*;
import java.awt.*;

/**
 * Factory centralizzata per la creazione dei pannelli.
 */
public final class PannelloFactory {

    private PannelloFactory() {
    }

    /**
     * Crea il pannello del marketplace.
     */
    public static JPanel creaMarketplacePanel(JFrame frameChiamante) {
        MarketplaceController controller = new MarketplaceController();
        MarketplacePanel panel = new MarketplacePanel(frameChiamante, controller);
        controller.notificaOsservatori();
        return panel;
    }

    /**
     * Crea il pannello di login.
     */
    public static JPanel creaLoginPanel(JFrame frameChiamante) {
        AutenticazioneController controller = new AutenticazioneController();
        return new LoginPanel(frameChiamante, controller);
    }

    /**
     * Crea il pannello di registrazione.
     */
    public static JPanel creaRegisterPanel(JFrame frameChiamante) {
        return new RegisterPanel(frameChiamante);
    }

    /**
     * Crea il pannello corrispondente al ruolo dell'utente.
     */
    public static JPanel creaPannelloRuolo(JFrame frame, UtenteAutenticato utente) {
        return switch (utente.getRuolo()) {
            case PRODUTTORE -> creaProduttorePanel(frame, utente);
            case CURATORE -> creaCuratorePanel(frame, utente);
            case DISTRIBUTORE_TIPICITA -> creaDistributorePanel(frame, utente);
            case ANIMATORE -> creaAnimatorePanel(frame, utente);
            case ACQUIRENTE -> creaAcquirentePanel(frame, utente);
            case TRASFORMATORE -> creaTrasformatorePanel(frame, utente);

            default -> null;
        };
    }

    public static JPanel creaProduttorePanel(JFrame frame, UtenteAutenticato utente) {
        return new PannelloProduttore(utente);
    }

    public static JPanel creaCuratorePanel(JFrame frame, UtenteAutenticato utente) {
        return new PannelloCuratore(utente);
    }

    public static JPanel creaDistributorePanel(JFrame frame, UtenteAutenticato utente) {
        return new PannelloDistributore(utente);
    }

    public static JPanel creaAnimatorePanel(JFrame frame, UtenteAutenticato utente) {
        return new PannelloAnimatore(utente);
    }

    public static JPanel creaAcquirentePanel(JFrame frame, UtenteAutenticato utente) {
        return new PannelloAcquirente(utente);
    }

    public static JPanel creaTrasformatorePanel(JFrame frame, UtenteAutenticato utente) {
        return new PannelloTrasformatore(utente);
    }
}
