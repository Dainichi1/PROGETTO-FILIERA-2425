/* ==================================================================== */
/*  PannelloFactory.java                                                */
/* ==================================================================== */
package unicam.filiera.factory;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.view.MarketplacePanel;

import javax.swing.*;

public final class PannelloFactory {

    private PannelloFactory() {}          // utility-class

    public static JPanel creaMarketplacePanel(JFrame frameChiamante) {


        MarketplaceController controller = new MarketplaceController();
        MarketplacePanel      panel      = new MarketplacePanel(frameChiamante, controller);


        controller.notificaOsservatori();

        return panel;
    }
}
