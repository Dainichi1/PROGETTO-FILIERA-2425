package unicam.filiera.view;

import unicam.filiera.controller.AnimatoreController;
import unicam.filiera.controller.EliminazioneProfiloController;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PannelloAnimatore extends JPanel {
    private final AnimatoreController controller;
    private final EliminazioneProfiloController eliminaController;

    private final JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Fiera", "Visita su invito"});

    private final JTextField txtF_DataInizio = new JTextField(15);
    private final JTextField txtF_DataFine = new JTextField(15);
    private final JTextField txtF_Prezzo = new JTextField(10);
    private final JTextField txtF_MinPartecipanti = new JTextField(5);
    private final JTextArea txtF_Descrizione = new JTextArea(3, 20);
    private final JTextField txtF_Indirizzo = new JTextField(20);

    private final JTextField txtV_DataInizio = new JTextField(15);
    private final JTextField txtV_DataFine = new JTextField(15);
    private final JTextField txtV_Prezzo = new JTextField(10);
    private final JTextField txtV_MinPartecipanti = new JTextField(5);
    private final JTextArea txtV_Descrizione = new JTextArea(3, 20);
    private final JTextField txtV_Indirizzo = new JTextField(20);

    private final Map<JCheckBox, UtenteAutenticato> destinatariMap = new LinkedHashMap<>();
    private final JPanel formContainer = new JPanel(new CardLayout());
    private final JButton btnEliminaProfilo = new JButton("Elimina profilo");


    public PannelloAnimatore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.controller = new AnimatoreController(utente.getUsername());
        this.eliminaController = new EliminazioneProfiloController(utente.getUsername());


        JLabel header = new JLabel("Benvenuto Animatore " + utente.getNome(), SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        JPanel top = new JPanel();
        top.add(new JLabel("Tipo evento:"));
        top.add(comboTipo);
        top.add(btnEliminaProfilo);
        add(top, BorderLayout.SOUTH);

        formContainer.add(buildFormFiera(), "Fiera");
        formContainer.add(buildFormVisita(), "Visita su invito");
        add(formContainer, BorderLayout.CENTER);

        setupComboTipoListener();
        comboTipo.setSelectedItem("Fiera");
        btnEliminaProfilo.addActionListener(e -> mostraDialogEliminaProfilo());

    }

    private void mostraDialogEliminaProfilo() {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler eliminare il tuo profilo?\nLa richiesta sarà inviata al Gestore.",
                "Conferma eliminazione profilo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (res != JOptionPane.YES_OPTION) return;

        eliminaController.inviaRichiestaEliminazione((ok, msg) -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        this, msg,
                        ok ? "Richiesta inviata" : "Errore",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                );
            });
        });
    }

    private void setupComboTipoListener() {
        comboTipo.addActionListener(e -> {
            CardLayout cl = (CardLayout) formContainer.getLayout();
            cl.show(formContainer, (String) comboTipo.getSelectedItem());
        });
    }

    private JPanel buildFormFiera() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = creaGbc();

        aggiungiRiga(p, gbc, 0, "Data Inizio (YYYY-MM-DD):", txtF_DataInizio);
        aggiungiRiga(p, gbc, 1, "Data Fine   (YYYY-MM-DD):", txtF_DataFine);
        aggiungiRiga(p, gbc, 2, "Prezzo:", txtF_Prezzo);
        aggiungiRiga(p, gbc, 3, "Min. Partecipanti:", txtF_MinPartecipanti);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(new JLabel("Descrizione:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        p.add(new JScrollPane(txtF_Descrizione), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        gbc.gridy = 5;
        gbc.gridx = 0;
        p.add(new JLabel("Indirizzo:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        p.add(txtF_Indirizzo, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        JPanel btnsF = new JPanel();
        JButton bPubF = new JButton("Pubblica Fiera");
        JButton bAnnF = new JButton("Annulla");
        btnsF.add(bPubF);
        btnsF.add(bAnnF);
        p.add(btnsF, gbc);

        bPubF.addActionListener(e -> {
            var dto = new FieraDto(
                    txtF_DataInizio.getText().trim(),
                    txtF_DataFine.getText().trim(),
                    txtF_Prezzo.getText().trim(),
                    txtF_Descrizione.getText().trim(),
                    txtF_Indirizzo.getText().trim(),
                    txtF_MinPartecipanti.getText().trim()
            );
            controller.inviaFiera(dto, (ok, msg) -> SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        this, msg,
                        ok ? "Successo" : "Errore",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                );
                if (ok) resetFieraForm();
            }));
        });
        bAnnF.addActionListener(e -> resetFieraForm());

        return p;
    }

    private JPanel buildFormVisita() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = creaGbc();

        aggiungiRiga(p, gbc, 0, "Data Inizio (YYYY-MM-DD):", txtV_DataInizio);
        aggiungiRiga(p, gbc, 1, "Data Fine   (YYYY-MM-DD):", txtV_DataFine);
        aggiungiRiga(p, gbc, 2, "Prezzo:", txtV_Prezzo);
        aggiungiRiga(p, gbc, 3, "Min. Partecipanti:", txtV_MinPartecipanti);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(new JLabel("Descrizione:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        p.add(new JScrollPane(txtV_Descrizione), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        gbc.gridy = 5;
        gbc.gridx = 0;
        p.add(new JLabel("Indirizzo:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        p.add(txtV_Indirizzo, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Seleziona destinatari:"), gbc);

        List<UtenteAutenticato> utenti = controller.getUtentiPerRuoli(
                Ruolo.PRODUTTORE,
                Ruolo.TRASFORMATORE,
                Ruolo.DISTRIBUTORE_TIPICITA
        );
        destinatariMap.clear();
        JPanel chkPanel = new JPanel(new GridLayout(utenti.size(), 1));
        for (UtenteAutenticato u : utenti) {
            JCheckBox cb = new JCheckBox(u.getNome() + " (" + u.getUsername() + ")");
            destinatariMap.put(cb, u);
            chkPanel.add(cb);
        }
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(new JScrollPane(chkPanel), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        JPanel btnsV = new JPanel();
        JButton bPubV = new JButton("Pubblica Visita");
        JButton bAnnV = new JButton("Annulla");
        btnsV.add(bPubV);
        btnsV.add(bAnnV);
        p.add(btnsV, gbc);

        bPubV.addActionListener(e -> {
            List<String> dest = destinatariMap.entrySet().stream()
                    .filter(entry -> entry.getKey().isSelected())
                    .map(entry -> entry.getValue().getUsername())
                    .toList();
            var dto = new VisitaInvitoDto(
                    txtV_DataInizio.getText().trim(),
                    txtV_DataFine.getText().trim(),
                    txtV_Prezzo.getText().trim(),
                    txtV_Descrizione.getText().trim(),
                    txtV_Indirizzo.getText().trim(),
                    txtV_MinPartecipanti.getText().trim(),
                    dest
            );
            controller.inviaVisitaInvito(dto, (ok, msg) -> SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        this, msg,
                        ok ? "Successo" : "Errore",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                );
                if (ok) resetVisitaForm();
            }));
        });
        bAnnV.addActionListener(e -> resetVisitaForm());

        return p;
    }

    private void aggiungiRiga(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        p.add(field, gbc);
        gbc.weightx = 0;
    }

    private GridBagConstraints creaGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        return gbc;
    }

    private void resetFieraForm() {
        txtF_DataInizio.setText("");
        txtF_DataFine.setText("");
        txtF_Prezzo.setText("");
        txtF_MinPartecipanti.setText("");
        txtF_Descrizione.setText("");
        txtF_Indirizzo.setText("");
        ((CardLayout) formContainer.getLayout()).show(formContainer, "Fiera");
    }

    private void resetVisitaForm() {
        txtV_DataInizio.setText("");
        txtV_DataFine.setText("");
        txtV_Prezzo.setText("");
        txtV_MinPartecipanti.setText("");
        txtV_Descrizione.setText("");
        txtV_Indirizzo.setText("");
        destinatariMap.keySet().forEach(cb -> cb.setSelected(false));
    }
}
