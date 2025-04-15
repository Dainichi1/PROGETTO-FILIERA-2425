package unicam.filiera.view;

import unicam.filiera.controller.AutenticazioneController;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;

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
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            Ruolo ruolo = (Ruolo) ruoloComboBox.getSelectedItem();

            UtenteAutenticato utente = controller.login(username, password);

            if (utente == null) {
                JOptionPane.showMessageDialog(this, "Credenziali errate", "Errore", JOptionPane.ERROR_MESSAGE);
            } else if (utente.getRuolo() != ruolo) {
                JOptionPane.showMessageDialog(this, "Ruolo non corrispondente a questo utente", "Errore", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                        "Accesso riuscito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Passaggio a finestra dedicata al ruolo (es. produttore)
                parentFrame.setContentPane(new PannelloProduttore(utente));
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        btnIndietro.addActionListener(e -> {
            if (parentFrame instanceof MainWindow mainWindow) {
                mainWindow.tornaAllaHome();
            }
        });

    }
}
