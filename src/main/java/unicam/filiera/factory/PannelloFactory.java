package unicam.filiera.factory;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.view.MarketplacePanel;

import javax.swing.*;

public class PannelloFactory {

    public static JPanel creaMarketplacePanel(JFrame frameChiamante) {
        MarketplaceController controller = new MarketplaceController();
        MarketplacePanel panel = new MarketplacePanel(frameChiamante, controller);

        controller.registraOsservatore(panel::mostraMarketplace);
        controller.notificaOsservatori();

        return panel;
    }

}
