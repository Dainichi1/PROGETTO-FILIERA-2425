package unicam.filiera.factory;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.view.MarketplacePanel;

import javax.swing.*;

public class PannelloFactory {

    public static JPanel creaMarketplacePanel() {
        MarketplaceController controller = new MarketplaceController();
        MarketplacePanel panel = new MarketplacePanel(controller);

        // Collega la view come osservatore
        controller.registraOsservatore(panel::mostraProdotti);

        // Inizializza con dati
        controller.notificaOsservatori();

        return panel;
    }

}
