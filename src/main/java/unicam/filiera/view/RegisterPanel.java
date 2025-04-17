package unicam.filiera.view;

import unicam.filiera.controller.AutenticazioneController;
import unicam.filiera.controller.RegistrazioneEsito;
import unicam.filiera.model.Ruolo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;


import static unicam.filiera.controller.RegistrazioneEsito.*;

public class RegisterPanel extends JPanel {

    public RegisterPanel(JFrame parentFrame) {
        setLayout(new GridLayout(8, 2, 10, 10));

        // Campi input
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nomeField = new JTextField();
        JTextField cognomeField = new JTextField();

        JComboBox<Ruolo> ruoloComboBox = new JComboBox<>(
                Arrays.stream(Ruolo.values())
                        .filter(Ruolo::isVisibile)
                        .toArray(Ruolo[]::new)
        );

        // Pulsanti
        JButton btnRegistrati = new JButton("Registrati");
        JButton btnIndietro = new JButton("Indietro");

        // Aggiunta componenti
        add(new JLabel("Username:"));
        add(usernameField);

        add(new JLabel("Password:"));
        add(passwordField);

        add(new JLabel("Nome:"));
        add(nomeField);

        add(new JLabel("Cognome:"));
        add(cognomeField);



        add(new JLabel("Ruolo:"));
        add(ruoloComboBox);

        add(btnRegistrati);
        add(btnIndietro);

        // Controller
        AutenticazioneController controller = new AutenticazioneController();

        // Azione: registrazione
        btnRegistrati.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            Ruolo ruolo = (Ruolo) ruoloComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || nome.isEmpty() || cognome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Compila tutti i campi!", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            RegistrazioneEsito esito = controller.registrati(username, password, nome, cognome, ruolo);

            switch (esito) {
                case SUCCESSO -> {
                    JOptionPane.showMessageDialog(this, "Registrazione completata!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.setContentPane(new MainWindow().getContentPane());
                    parentFrame.revalidate();
                }
                case USERNAME_GIA_ESISTENTE ->
                        JOptionPane.showMessageDialog(this, "Username già registrato. Scegli un altro.", "Errore", JOptionPane.ERROR_MESSAGE);
                case PERSONA_GIA_REGISTRATA ->
                        JOptionPane.showMessageDialog(this, "Questa persona è già registrata con un altro ruolo!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Azione: torna alla schermata principale
        btnIndietro.addActionListener(e -> {
            if (parentFrame instanceof MainWindow mainWindow) {
                mainWindow.tornaAllaHome();
            }
        });



    }
}
