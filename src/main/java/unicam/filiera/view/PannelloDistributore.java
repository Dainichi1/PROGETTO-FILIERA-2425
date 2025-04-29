package unicam.filiera.view;

import unicam.filiera.controller.DistributoreController;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.observer.OsservatorePacchetto;
import unicam.filiera.model.observer.PacchettoNotifier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * View pura: presenta l’interfaccia e delega la logica
 * al DistributoreController.
 */
public class PannelloDistributore extends JPanel implements OsservatorePacchetto {

    /* ------------------------------------------------------------------ */
    /*  D A T A                                                            */
    /* ------------------------------------------------------------------ */
    private final UtenteAutenticato utente;
    private final DistributoreController controller;

    /*  cache locale di selezioni utente (solo UI)  */
    private final List<File> certificatiSel = new ArrayList<>();
    private final List<File> fotoSel        = new ArrayList<>();
    private final List<Prodotto> prodottiSel = new ArrayList<>();

    /* ------------------------------------------------------------------ */
    /*  C O M P O N E N T I                                                */
    /* ------------------------------------------------------------------ */
    private final JTextField nomeField        = new JTextField();
    private final JTextField descrizioneField = new JTextField();
    private final JTextField indirizzoField   = new JTextField();
    private final JTextField prezzoField      = new JTextField();

    private final JLabel labelCert = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm     = new JButton("Chiudi form");
    private final JButton btnSelezionaProd  = new JButton("Seleziona Prodotti");
    private final JButton btnCertificati    = new JButton("Seleziona Certificati");
    private final JButton btnFoto           = new JButton("Seleziona Foto");
    private final JButton btnInviaPacchetto = new JButton("Invia Pacchetto");

    private final DefaultTableModel modelProdotti;
    private final JTable            tabellaProdotti;
    private final DefaultTableModel modelPacchetti;
    private final JTable            tabellaPacchetti;

    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private boolean formVisibile  = true;

    /* ------------------------------------------------------------------ */
    /*  C O S T R U T T O R E                                              */
    /* ------------------------------------------------------------------ */
    public PannelloDistributore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;
        this.controller = new DistributoreController(utente.getUsername());

        /* ---------- header ---------- */
        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        add(benv, BorderLayout.NORTH);

        /* ---------- form creazione pacchetto ---------- */
        buildForm();

        /* ---------- tabelle ---------- */
        String[] colProd = {"Nome","Descrizione","Quantità","Prezzo","Indirizzo"};
        modelProdotti    = new DefaultTableModel(colProd, 0);
        tabellaProdotti  = new JTable(modelProdotti);

        String[] colPack = {"Nome","Descrizione","Indirizzo","Prezzo",
                "Prodotti","Certificati","Foto","Stato","Commento"};
        modelPacchetti   = new DefaultTableModel(colPack, 0);
        tabellaPacchetti = new JTable(modelPacchetti);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabellaProdotti),
                new JScrollPane(tabellaPacchetti));
        split.setDividerLocation(250);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Prodotti e Pacchetti:", SwingConstants.CENTER), BorderLayout.NORTH);
        right.add(split, BorderLayout.CENTER);
        right.add(btnSelezionaProd, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        /* ---------- event wiring ---------- */
        wireEvents();

        /* ---------- dati iniziali ---------- */
        refreshProdotti();
        refreshPacchetti();

        /* ---------- observer ---------- */
        PacchettoNotifier.getInstance().registraOsservatore(this);
    }

    /* ======================================================================
       COSTRUZIONE UI
       ====================================================================== */
    private void buildForm() {
        formPanel.add(new JLabel("Nome Pacchetto:"));        formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrizione:"));           formPanel.add(descrizioneField);
        formPanel.add(new JLabel("Indirizzo luogo vendita:"));formPanel.add(indirizzoField);
        formPanel.add(new JLabel("Prezzo Totale:"));         formPanel.add(prezzoField);
        formPanel.add(btnCertificati);       formPanel.add(labelCert);
        formPanel.add(btnFoto);              formPanel.add(labelFoto);
        formPanel.add(btnToggleForm);        formPanel.add(btnInviaPacchetto);
        add(formPanel, BorderLayout.CENTER);
    }

    private void wireEvents() {

        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Pacchetto");
            revalidate(); repaint();
        });

        btnSelezionaProd.addActionListener(e -> {
            int[] rows = tabellaProdotti.getSelectedRows();
            prodottiSel.clear();
            for (int r : rows) prodottiSel.add(
                    controller.getProdottiMarketplace().get(r));
            JOptionPane.showMessageDialog(this,
                    rows.length + " prodotto/i selezionato/i.",
                    "Selezione Prodotti", JOptionPane.INFORMATION_MESSAGE);
        });

        btnCertificati.addActionListener(e -> pickFiles(true));
        btnFoto.addActionListener(e -> pickFiles(false));

        btnInviaPacchetto.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Inviare il pacchetto al curatore per approvazione?",
                    "Conferma invio",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) sendToController();
        });
    }

    /* ======================================================================
       OPERAZIONI DELLA VIEW
       ====================================================================== */
    private void pickFiles(boolean certificati) {
        JFileChooser ch = new JFileChooser();
        ch.setMultiSelectionEnabled(true);
        if (ch.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        List<File> target = certificati ? certificatiSel : fotoSel;
        target.clear();
        target.addAll(List.of(ch.getSelectedFiles()));
        (certificati ? labelCert : labelFoto)
                .setText(target.size() + " file selezionati");
    }

    private void sendToController() {
        controller.inviaPacchetto(
                nomeField.getText().trim(),
                descrizioneField.getText().trim(),
                indirizzoField.getText().trim(),
                prezzoField.getText().trim(),     // verrà convertito/validato dal controller
                prodottiSel, certificatiSel, fotoSel,
                /* callback UI-safe */
                (ok, message) -> SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, message,
                            ok ? "Successo" : "Errore",
                            ok ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.ERROR_MESSAGE);
                    if (ok) { resetForm(); refreshPacchetti(); }
                }));
    }

    /* ======================================================================
       RENDER DATI
       ====================================================================== */
    private void refreshProdotti() {
        modelProdotti.setRowCount(0);
        for (Prodotto p : controller.getProdottiMarketplace()) {
            modelProdotti.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(),
                    p.getQuantita(), p.getPrezzo(), p.getIndirizzo()});
        }
    }

    private void refreshPacchetti() {
        modelPacchetti.setRowCount(0);
        for (Pacchetto p : controller.getPacchettiCreatiDaMe()) {
            modelPacchetti.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(), p.getIndirizzo(),
                    p.getPrezzoTotale(), p.getProdotti().size() + " prodotti",
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato() != null ? p.getStato().name() : "N/D",
                    p.getCommento() != null ? p.getCommento() : ""});
        }
    }

    /* ======================================================================
       OBSERVER
       ====================================================================== */
    @Override
    public void notifica(Pacchetto pacchetto, String evento) {
        if (!pacchetto.getCreatoDa().equalsIgnoreCase(utente.getUsername())) return;
        SwingUtilities.invokeLater(this::refreshPacchetti);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        PacchettoNotifier.getInstance().rimuoviOsservatore(this);
    }

    /* ======================================================================
       UTILITY
       ====================================================================== */
    private void resetForm() {
        nomeField.setText("");
        descrizioneField.setText("");
        indirizzoField.setText("");
        prezzoField.setText("");
        certificatiSel.clear(); fotoSel.clear(); prodottiSel.clear();
        labelCert.setText("Nessun file selezionato");
        labelFoto.setText("Nessun file selezionato");
    }
}
