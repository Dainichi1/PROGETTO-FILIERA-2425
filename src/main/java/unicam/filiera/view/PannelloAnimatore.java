package unicam.filiera.view;

import unicam.filiera.controller.AnimatoreController;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.model.UtenteAutenticato;

import javax.swing.*;
import java.awt.*;

public class PannelloAnimatore extends JPanel {
    private final AnimatoreController controller;
    private final JButton btnToggleForm = new JButton("Crea Fiera");

    // campi del form
    private final JTextField txtDataInizio        = new JTextField();
    private final JTextField txtDataFine          = new JTextField();
    private final JTextField txtPrezzo            = new JTextField();
    private final JTextField txtMinPartecipanti   = new JTextField();
    private final JTextArea  txtDescrizione       = new JTextArea(3, 20);
    private final JTextField txtIndirizzo         = new JTextField();

    private final JPanel formPanel;

    public PannelloAnimatore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.controller = new AnimatoreController(utente.getUsername());

        // header
        JLabel header = new JLabel(
                "Benvenuto Animatore " + utente.getNome(),
                SwingConstants.CENTER
        );
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // bottone per mostrare/nascondere il form
        JPanel top = new JPanel();
        top.add(btnToggleForm);
        add(top, BorderLayout.SOUTH);

        // form (inizialmente nascosto)
        formPanel = buildFormPanel();
        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);

        // wiring del toggle
        btnToggleForm.addActionListener(e -> {
            boolean vis = !formPanel.isVisible();
            formPanel.setVisible(vis);
            btnToggleForm.setText(vis ? "Chiudi form" : "Crea Fiera");
            revalidate();
        });
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        // data inizio
        p.add(new JLabel("Data Inizio (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; p.add(txtDataInizio, gbc);

        // data fine
        gbc.gridy++; gbc.gridx = 0;
        p.add(new JLabel("Data Fine (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; p.add(txtDataFine, gbc);

        // prezzo
        gbc.gridy++; gbc.gridx = 0;
        p.add(new JLabel("Prezzo:"), gbc);
        gbc.gridx = 1; p.add(txtPrezzo, gbc);

        // numero minimo partecipanti
        gbc.gridy++; gbc.gridx = 0;
        p.add(new JLabel("Min. Partecipanti:"), gbc);
        gbc.gridx = 1; p.add(txtMinPartecipanti, gbc);

        // descrizione
        gbc.gridy++; gbc.gridx = 0;
        p.add(new JLabel("Descrizione:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        p.add(new JScrollPane(txtDescrizione), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // indirizzo
        gbc.gridy++; gbc.gridx = 0;
        p.add(new JLabel("Indirizzo:"), gbc);
        gbc.gridx = 1; p.add(txtIndirizzo, gbc);

        // pulsanti
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel btns = new JPanel();
        JButton btnPubblica = new JButton("Pubblica Fiera");
        JButton btnAnnulla  = new JButton("Annulla");
        btns.add(btnPubblica);
        btns.add(btnAnnulla);
        p.add(btns, gbc);

        // azioni dei pulsanti
        btnPubblica.addActionListener(e -> onPubblica());
        btnAnnulla .addActionListener(e -> {
            formPanel.setVisible(false);
            btnToggleForm.setText("Crea Fiera");
        });

        return p;
    }

    private void onPubblica() {
        FieraDto dto = new FieraDto(
                txtDataInizio.getText().trim(),
                txtDataFine  .getText().trim(),
                txtPrezzo    .getText().trim(),
                txtDescrizione.getText().trim(),
                txtIndirizzo .getText().trim(),
                txtMinPartecipanti.getText().trim()
        );


        controller.inviaFiera(dto, (ok, msg) -> SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this, msg,
                    ok ? "Successo" : "Errore",
                    ok ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.ERROR_MESSAGE
            );
            if (ok) {
                // reset form…
                txtDataInizio.setText("");
                txtDataFine.  setText("");
                txtPrezzo.    setText("");
                txtMinPartecipanti.setText("");
                txtDescrizione.setText("");
                txtIndirizzo. setText("");
                formPanel.setVisible(false);
                btnToggleForm.setText("Crea Fiera");
            }
        }));
    }
}
