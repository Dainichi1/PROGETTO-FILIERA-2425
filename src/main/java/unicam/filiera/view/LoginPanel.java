package unicam.filiera.view;

import unicam.filiera.controller.AutenticazioneController;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.util.ValidatoreLogin;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    public LoginPanel(JFrame parentFrame) {
        setLayout(new GridLayout(5, 2, 10, 10));

        // Campi input
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<Ruolo> ruoloComboBox = new JComboBox<>(Ruolo.values());

        // Pulsanti
        JButton btnLogin = new JButton("Login");
        JButton btnIndietro = new JButton("Indietro");

        add(new JLabel("Username:"));
        add(usernameField);

        add(new JLabel("Password:"));
        add(passwordField);

        add(new JLabel("Ruolo:"));
        add(ruoloComboBox);

        add(btnLogin);
        add(btnIndietro);

        AutenticazioneController controller = new AutenticazioneController();

        btnLogin.addActionListener(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                Ruolo ruolo = (Ruolo) ruoloComboBox.getSelectedItem();

                // Validazione con utility separata
                ValidatoreLogin.valida(username, password);

                UtenteAutenticato utente = controller.login(username, password);

                if (utente == null) {
                    JOptionPane.showMessageDialog(this, "Credenziali errate", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (utente.getRuolo() != ruolo) {
                    JOptionPane.showMessageDialog(this, "Ruolo non corrispondente a questo utente", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this,
                        "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                        "Accesso riuscito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Apri nuova finestra in base al ruolo
                JFrame nuovaFinestra = new JFrame("Dashboard - " + utente.getRuolo());
                nuovaFinestra.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                nuovaFinestra.setSize(900, 600);
                nuovaFinestra.setLocationRelativeTo(null);

                switch (utente.getRuolo()) {
                    case PRODUTTORE -> nuovaFinestra.setContentPane(new PannelloProduttore(utente));
                    case CURATORE -> nuovaFinestra.setContentPane(new PannelloCuratore(utente));
                    case DISTRIBUTORE_TIPICITA -> nuovaFinestra.setContentPane(new PannelloDistributore(utente));

                    default -> {
                        JOptionPane.showMessageDialog(this,
                                "Nessun pannello associato a questo ruolo.",
                                "Ruolo non gestito",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                nuovaFinestra.setVisible(true);

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore imprevisto: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnIndietro.addActionListener(e -> {
            if (parentFrame instanceof MainWindow mainWindow) {
                mainWindow.tornaAllaHome();
            }
        });
    }
}
