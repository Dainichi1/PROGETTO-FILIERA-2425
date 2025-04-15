package unicam.filiera.view;

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
            JOptionPane.showMessageDialog(this, "Marketplace non ancora implementato.");
        });
    }

    public void tornaAllaHome() {
        setContentPane(homePanel);
        revalidate();
        repaint();
    }
}
