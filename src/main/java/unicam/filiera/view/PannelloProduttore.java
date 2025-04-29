package unicam.filiera.view;

import unicam.filiera.controller.ObserverManager;
import unicam.filiera.controller.ProduttoreController;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.observer.OsservatoreProdotto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * View (solo grafica) per il ruolo «Produttore».
 * Tutta la logica applicativa/di persistenza è delegata a {@link ProduttoreController}.
 */
public class PannelloProduttore extends JPanel implements OsservatoreProdotto {

    /* ====================================================================
       D A T A
       ==================================================================== */
    private final UtenteAutenticato    utente;
    private final ProduttoreController controller;          // logica

    /* cache selezioni utente (vive solo nella UI) */
    private final List<File> certSel  = new ArrayList<>();
    private final List<File> fotoSel  = new ArrayList<>();

    /* ====================================================================
       C O M P O N E N T I
       ==================================================================== */
    private final JTextField nomeField      = new JTextField();
    private final JTextField descrField     = new JTextField();
    private final JTextField quantField     = new JTextField();
    private final JTextField prezzoField    = new JTextField();
    private final JTextField indirizzoField = new JTextField();

    private final JLabel  labelCert = new JLabel("Nessun file selezionato");
    private final JLabel  labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Crea Prodotto");
    private final JButton btnCert       = new JButton("Seleziona certificati");
    private final JButton btnFoto       = new JButton("Seleziona foto");
    private final JButton btnInvia      = new JButton("Invia al Curatore");

    private final DefaultTableModel model;
    private final JTable            tabella;

    private final JPanel  formPanel  = new JPanel(new GridLayout(8,2,10,10));
    private       boolean formVisibile = false;

    /* ====================================================================
       C O S T R U T T O R E
       ==================================================================== */
    public PannelloProduttore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente      = utente;
        this.controller  = new ProduttoreController(utente.getUsername());   // <-- username passato al controller

        /* ------------- Header ------------- */
        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        add(benv, BorderLayout.NORTH);

        /* ------------- Form --------------- */
        buildForm();

        /* ------------- Tabella ------------ */
        String[] cols = {"Nome","Descrizione","Qtà","Prezzo","Indirizzo",
                "Certificati","Foto","Stato","Commento"};
        model   = new DefaultTableModel(cols, 0);
        tabella = new JTable(model);
        add(new JScrollPane(tabella), BorderLayout.EAST);

        /* ------------- Toggle visibilità form ------------- */
        add(btnToggleForm, BorderLayout.SOUTH);
        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto");
            revalidate(); repaint();
        });

        /* ------------- File chooser ------------ */
        btnCert.addActionListener(e -> chooseFiles(true));
        btnFoto.addActionListener(e -> chooseFiles(false));

        /* ------------- Invio ------------- */
        btnInvia.addActionListener(e -> confermaEInvia());

        /* ------------- Dati iniziali ------------- */
        refreshTable();

        /* ------------- Observer ------------- */
        ObserverManager.registraOsservatore(this);
    }

    /* ====================================================================
       COSTRUZIONE UI
       ==================================================================== */
    private void buildForm() {
        formPanel.add(new JLabel("Nome prodotto:"));               formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrizione:"));                 formPanel.add(descrField);
        formPanel.add(new JLabel("Quantità:"));                    formPanel.add(quantField);
        formPanel.add(new JLabel("Prezzo:"));                      formPanel.add(prezzoField);
        formPanel.add(new JLabel("Indirizzo luogo vendita:"));     formPanel.add(indirizzoField);
        formPanel.add(btnCert);  formPanel.add(labelCert);
        formPanel.add(btnFoto);  formPanel.add(labelFoto);
        formPanel.add(new JLabel()); formPanel.add(btnInvia);      // colonna vuota per layout
        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);
    }

    /* ====================================================================
       EVENTI UI
       ==================================================================== */
    private void chooseFiles(boolean certificati) {
        JFileChooser ch = new JFileChooser();
        ch.setMultiSelectionEnabled(true);
        if (ch.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        if (certificati) {
            certSel.clear(); certSel.addAll(List.of(ch.getSelectedFiles()));
            labelCert.setText(certSel.size() + " file selezionati");
        } else {
            fotoSel.clear(); fotoSel.addAll(List.of(ch.getSelectedFiles()));
            labelFoto.setText(fotoSel.size() + " file selezionati");
        }
    }

    private void confermaEInvia() {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Inviare il prodotto al curatore per approvazione?",
                "Conferma invio",
                JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;

        /* --- delega la logica al controller ---- */
        controller.inviaProdotto(
                nomeField.getText().trim(),
                descrField.getText().trim(),
                quantField.getText().trim(),
                prezzoField.getText().trim(),
                indirizzoField.getText().trim(),
                certSel, fotoSel,
                /* callback UI-safe */
                (success, msg) -> SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, msg,
                            success ? "Successo" : "Errore",
                            success ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.ERROR_MESSAGE);
                    if (success) { resetForm(); refreshTable(); }
                }));
    }

    /* ====================================================================
       RENDER / REFRESH
       ==================================================================== */
    private void refreshTable() {
        model.setRowCount(0);
        for (Prodotto p : controller.getProdottiCreatiDaMe()) {
            model.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(), p.getQuantita(),
                    p.getPrezzo(), p.getIndirizzo(),
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato() != null ? p.getStato().name() : "N/D",
                    p.getCommento() != null ? p.getCommento() : ""});
        }
    }

    private void resetForm() {
        nomeField.setText("");   descrField.setText("");
        quantField.setText("");  prezzoField.setText("");
        indirizzoField.setText("");
        certSel.clear(); fotoSel.clear();
        labelCert.setText("Nessun file selezionato");
        labelFoto.setText("Nessun file selezionato");
    }

    /* ====================================================================
       O B S E R V E R
       ==================================================================== */
    @Override
    public void notifica(Prodotto prod, String evento) {
        if (!prod.getCreatoDa().equalsIgnoreCase(utente.getUsername())) return;

        SwingUtilities.invokeLater(() -> {
            if ("APPROVATO".equals(evento)) {
                JOptionPane.showMessageDialog(this,
                        "✔ Il tuo prodotto \"" + prod.getNome() + "\" è stato APPROVATO!",
                        "Prodotto approvato", JOptionPane.INFORMATION_MESSAGE);
            } else if ("RIFIUTATO".equals(evento)) {
                String msg = "❌ Il tuo prodotto \"" + prod.getNome() + "\" è stato RIFIUTATO.";
                if (prod.getCommento() != null && !prod.getCommento().isBlank())
                    msg += "\nCommento del curatore: " + prod.getCommento();
                JOptionPane.showMessageDialog(this, msg,
                        "Prodotto rifiutato", JOptionPane.WARNING_MESSAGE);
            }
            refreshTable();
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this);
    }
}
