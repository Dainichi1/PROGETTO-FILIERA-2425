package unicam.filiera.view;

import unicam.filiera.controller.ProduttoreController;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.controller.ObserverManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * View rifattorizzata per il ruolo «Produttore».
 * Utilizza ProdottoDto e il controller moderno.
 */
public class PannelloProduttore extends JPanel implements OsservatoreProdotto {

    private final UtenteAutenticato utente;
    private final ProduttoreController controller;

    private final List<File> certSel = new ArrayList<>();
    private final List<File> fotoSel = new ArrayList<>();

    private final JTextField nomeField = new JTextField();
    private final JTextField descrField = new JTextField();
    private final JTextField quantField = new JTextField();
    private final JTextField prezzoField = new JTextField();
    private final JTextField indirizzoField = new JTextField();

    private final JLabel labelCert = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Crea Prodotto");
    private final JButton btnCert = new JButton("Seleziona certificati");
    private final JButton btnFoto = new JButton("Seleziona foto");
    private final JButton btnInvia = new JButton("Invia Prodotto");

    private final DefaultTableModel model;
    private final JTable tabella;
    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private boolean formVisibile = false;

    public PannelloProduttore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;
        this.controller = new ProduttoreController(utente.getUsername());

        // Header
        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        add(benv, BorderLayout.NORTH);

        // Form panel
        buildForm();
        add(btnToggleForm, BorderLayout.SOUTH);
        btnToggleForm.addActionListener(e -> toggleForm());

        // Table panel
        String[] cols = {"Nome","Descrizione","Qtà","Prezzo","Indirizzo",
                "Certificati","Foto","Stato","Commento"};
        model = new DefaultTableModel(cols, 0);
        tabella = new JTable(model);
        add(new JScrollPane(tabella), BorderLayout.EAST);

        // File selectors
        btnCert.addActionListener(e -> chooseFiles(true));
        btnFoto.addActionListener(e -> chooseFiles(false));

        // Submit
        btnInvia.addActionListener(e -> submit());

        // Initial load
        refreshTable();

        // Observer
        ObserverManager.registraOsservatore(this);
    }

    private void buildForm() {
        formPanel.add(new JLabel("Nome prodotto:"));      formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrizione:"));        formPanel.add(descrField);
        formPanel.add(new JLabel("Quantità:"));           formPanel.add(quantField);
        formPanel.add(new JLabel("Prezzo:"));             formPanel.add(prezzoField);
        formPanel.add(new JLabel("Indirizzo:"));          formPanel.add(indirizzoField);
        formPanel.add(btnCert);                            formPanel.add(labelCert);
        formPanel.add(btnFoto);                            formPanel.add(labelFoto);
        formPanel.add(new JLabel());                       formPanel.add(btnInvia);
        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);
    }

    private void toggleForm() {
        formVisibile = !formVisibile;
        formPanel.setVisible(formVisibile);
        btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto");
        revalidate(); repaint();
    }

    private void chooseFiles(boolean cert) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        if (cert) {
            certSel.clear(); certSel.addAll(List.of(chooser.getSelectedFiles()));
            labelCert.setText(certSel.size() + " file selezionati");
        } else {
            fotoSel.clear(); fotoSel.addAll(List.of(chooser.getSelectedFiles()));
            labelFoto.setText(fotoSel.size() + " file selezionati");
        }
    }

    private void submit() {
        ProdottoDto dto = new ProdottoDto(
                nomeField.getText().trim(),
                descrField.getText().trim(),
                quantField.getText().trim(),
                prezzoField.getText().trim(),
                indirizzoField.getText().trim(),
                List.copyOf(certSel),
                List.copyOf(fotoSel)
        );

        controller.inviaProdotto(dto, (success, msg) -> SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, msg,
                    success ? "Successo" : "Errore",
                    success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (success) {
                resetForm();
                refreshTable();
            }
        }));
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Prodotto p : controller.getProdottiCreatiDaMe()) {
            model.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(), p.getQuantita(),
                    p.getPrezzo(), p.getIndirizzo(),
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato(), p.getCommento()
            });
        }
    }

    private void resetForm() {
        nomeField.setText(""); descrField.setText("");
        quantField.setText(""); prezzoField.setText("");
        indirizzoField.setText(""); certSel.clear(); fotoSel.clear();
        labelCert.setText("Nessun file selezionato");
        labelFoto.setText("Nessun file selezionato");
    }

    @Override
    public void notifica(Prodotto prod, String evento) {
        // ignora gli eventi "NUOVO_PRODOTTO" o qualunque altro
        if (!"APPROVATO".equals(evento) && !"RIFIUTATO".equals(evento)) {
            return;
        }
        if (!prod.getCreatoDa().equalsIgnoreCase(utente.getUsername())) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            boolean approved = "APPROVATO".equals(evento);
            String title = approved ? "Prodotto approvato" : "Prodotto rifiutato";
            int type = approved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;

            String msg;
            if (approved) {
                msg = "✔ Il tuo prodotto \"" + prod.getNome() + "\" è stato APPROVATO!";
            } else {
                msg = "❌ Il tuo prodotto \"" + prod.getNome() + "\" è stato RIFIUTATO."
                        + (prod.getCommento() != null && !prod.getCommento().isBlank()
                        ? "\nCommento: " + prod.getCommento()
                        : "");
            }

            JOptionPane.showMessageDialog(this, msg, title, type);
            refreshTable();
        });
    }


    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this);
    }
}
