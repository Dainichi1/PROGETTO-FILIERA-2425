package unicam.filiera.view;

import unicam.filiera.factory.PannelloFactory;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final JPanel homePanel;
    private MarketplacePanel marketplacePanel;
    private boolean marketplaceVisibile = false;


    public MainWindow() {
        setTitle("Filiera Agricola - Benvenuto");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Pannello centrale con i pulsanti
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 15, 15));

        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Registrati");
        JButton btnMarketplace = new JButton("Visualizza Marketplace");

        panel.add(btnLogin);
        panel.add(btnRegister);
        panel.add(btnMarketplace);

        add(panel, BorderLayout.CENTER);

        this.homePanel = panel;

        // Listener per login
        btnLogin.addActionListener(e -> {
            setContentPane(new LoginPanel(this));
            revalidate();
            repaint();
        });

        // Listener per registrazione
        btnRegister.addActionListener(e -> {
            setContentPane(new RegisterPanel(this));
            revalidate();
            repaint();
        });

        // Marketplace (placeholder)
        btnMarketplace.addActionListener(e -> {
            JPanel marketplace = PannelloFactory.creaMarketplacePanel(this);
            setContentPane(marketplace);

            // Per tornare indietro alla home
            if (marketplace instanceof MarketplacePanel mp) {
                mp.getBtnIndietro().addActionListener(ev -> tornaAllaHome());
            }

            revalidate();
            repaint();
        });


    }

    public void tornaAllaHome() {
        setContentPane(homePanel);
        revalidate();
        repaint();
    }
}
