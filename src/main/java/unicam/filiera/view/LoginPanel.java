package unicam.filiera.view;

import unicam.filiera.controller.AutenticazioneController;
import unicam.filiera.factory.PannelloFactory;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.util.ValidatoreLogin;

import javax.swing.*;
import java.awt.*;


import unicam.filiera.controller.AutenticazioneController;
import unicam.filiera.factory.UtenteFactory;
import unicam.filiera.model.Utente;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Ruolo;
import unicam.filiera.util.ValidatoreLogin;

import javax.swing.*;
import java.awt.*;

/**
 * Pannello di login che delega l'autenticazione al controller e
 * gestisce la navigazione verso i vari dashboard in base al ruolo.
 */
public class LoginPanel extends JPanel {

    public LoginPanel(JFrame parentFrame, AutenticazioneController controller) {
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

        btnLogin.addActionListener(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                Ruolo ruolo = (Ruolo) ruoloComboBox.getSelectedItem();

                // Validazione dei campi
                ValidatoreLogin.valida(username, password);

                // Esegue il login tramite il controller
                Utente rawUser = controller.login(username, password);
                if (rawUser == null) {
                    JOptionPane.showMessageDialog(this,
                            "Credenziali errate",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!(rawUser instanceof UtenteAutenticato authUser) || authUser.getRuolo() != ruolo) {
                    JOptionPane.showMessageDialog(this,
                            "Ruolo non corrispondente a questo utente",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this,
                        "Benvenuto " + authUser.getNome() + ", " + authUser.getRuolo(),
                        "Accesso riuscito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Crea la finestra del dashboard in base al ruolo
                JFrame dashboard = new JFrame("Dashboard - " + authUser.getRuolo());
                dashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                dashboard.setSize(900, 600);
                dashboard.setLocationRelativeTo(null);

                switch (authUser.getRuolo()) {
                    case PRODUTTORE -> dashboard.setContentPane(
                            PannelloFactory.creaProduttorePanel(dashboard, authUser));
                    case CURATORE -> dashboard.setContentPane(
                            PannelloFactory.creaCuratorePanel(dashboard, authUser));
                    case DISTRIBUTORE_TIPICITA -> dashboard.setContentPane(
                            PannelloFactory.creaDistributorePanel(dashboard, authUser));
                    case ANIMATORE -> dashboard.setContentPane(
                            PannelloFactory.creaAnimatorePanel(dashboard, authUser));
                    case ACQUIRENTE -> dashboard.setContentPane(
                            PannelloFactory.creaAcquirentePanel(dashboard, authUser));
                    case TRASFORMATORE -> dashboard.setContentPane(
                            PannelloFactory.creaTrasformatorePanel(dashboard, authUser));
                    default -> {
                        JOptionPane.showMessageDialog(this,
                                "Nessun pannello associato a questo ruolo.",
                                "Ruolo non gestito",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                dashboard.setVisible(true);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Errore imprevisto: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnIndietro.addActionListener(e -> {
            if (parentFrame instanceof MainWindow main) {
                main.tornaAllaHome();
            }
        });
    }
}

