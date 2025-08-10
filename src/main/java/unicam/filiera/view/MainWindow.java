package unicam.filiera.view;

import unicam.filiera.factory.PannelloFactory;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final JPanel homePanel;

    public MainWindow() {
        setTitle("Filiera Agricola - Benvenuto");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Pannello centrale con i pulsanti
        JPanel panel = new JPanel(new GridLayout(4, 1, 15, 15));

        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Registrati");
        JButton btnMarketplace = new JButton("Visualizza Marketplace");
        JButton btnSocial = new JButton("Visualizza Social Network"); // NUOVO PULSANTE

        panel.add(btnLogin);
        panel.add(btnRegister);
        panel.add(btnMarketplace);
        panel.add(btnSocial); // aggiungo il nuovo bottone

        this.homePanel = panel;
        setContentPane(homePanel);

        // Listener per login
        btnLogin.addActionListener(e -> mostraPannello(PannelloFactory.creaLoginPanel(this)));

        // Listener per registrazione
        btnRegister.addActionListener(e -> mostraPannello(PannelloFactory.creaRegisterPanel(this)));

        // Marketplace
        btnMarketplace.addActionListener(e -> {
            JPanel marketplace = PannelloFactory.creaMarketplacePanel(this);
            mostraPannello(marketplace);

            if (marketplace instanceof MarketplacePanel mp) {
                mp.getBtnIndietro().addActionListener(ev -> tornaAllaHome());
            }
        });

        // Social Network
        btnSocial.addActionListener(e -> {
            SocialPanel socialPanel = new SocialPanel(this);
            mostraPannello(socialPanel);
            socialPanel.getBtnIndietro().addActionListener(ev -> tornaAllaHome());
        });

    }

    public void tornaAllaHome() {
        mostraPannello(homePanel);
    }

    private void mostraPannello(JPanel pannello) {
        SwingUtilities.invokeLater(() -> {
            setContentPane(pannello);
            revalidate();
            repaint();
        });
    }
}
