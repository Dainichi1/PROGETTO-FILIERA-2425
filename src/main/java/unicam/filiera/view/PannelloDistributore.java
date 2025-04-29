package unicam.filiera.view;

import unicam.filiera.controller.DistributoreController;
import unicam.filiera.controller.ObserverManagerPacchetto;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.observer.OsservatorePacchetto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * View pura: mostra l’interfaccia e delega la logica
 * a {@link DistributoreController}.
 */
public class PannelloDistributore extends JPanel implements OsservatorePacchetto {
    private final UtenteAutenticato utente;
    private final DistributoreController controller;

    private final List<File> certSel     = new ArrayList<>();
    private final List<File> fotoSel     = new ArrayList<>();
    private final List<Prodotto> prodottiSel = new ArrayList<>();

    private final JTextField nomeField      = new JTextField();
    private final JTextField descrField     = new JTextField();
    private final JTextField indirizzoField = new JTextField();
    private final JTextField prezzoField    = new JTextField();

    private final JLabel labelCert = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Chiudi form");
    private final JButton btnSelProd    = new JButton("Seleziona Prodotti");
    private final JButton btnCert       = new JButton("Seleziona Certificati");
    private final JButton btnFoto       = new JButton("Seleziona Foto");
    private final JButton btnInvia      = new JButton("Invia Pacchetto");

    private final DefaultTableModel modelProdotti;
    private final JTable            tabellaProdotti;
    private final DefaultTableModel modelPacchetti;
    private final JTable            tabellaPacchetti;

    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private boolean formVisibile = true;

    public PannelloDistributore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente     = utente;
        this.controller = new DistributoreController(utente.getUsername());

        // Header
        JLabel benvenuto = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER
        );
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        // Form building
        buildForm();

        // Prodotti table
        String[] colProd = {"Nome","Descrizione","Quantità","Prezzo","Indirizzo"};
        modelProdotti    = new DefaultTableModel(colProd, 0);
        tabellaProdotti  = new JTable(modelProdotti);

        // Pacchetti table
        String[] colPack = {
                "Nome","Descrizione","Indirizzo","Prezzo Totale",
                "Prodotti","Certificati","Foto","Stato","Commento"
        };
        modelPacchetti   = new DefaultTableModel(colPack, 0);
        tabellaPacchetti = new JTable(modelPacchetti);

        // Split pane
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabellaProdotti),
                new JScrollPane(tabellaPacchetti)
        );
        split.setDividerLocation(250);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Prodotti e Pacchetti:", SwingConstants.CENTER),
                BorderLayout.NORTH);
        right.add(split, BorderLayout.CENTER);
        right.add(btnSelProd, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        // Event wiring
        wireEvents();

        // Initial data load
        refreshProdotti();
        refreshPacchetti();

        // Observer registration for pacchetti
        ObserverManagerPacchetto.registraOsservatore(this);
    }

    private void buildForm() {
        formPanel.add(new JLabel("Nome Pacchetto:"));          formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrizione:"));             formPanel.add(descrField);
        formPanel.add(new JLabel("Indirizzo luogo vendita:")); formPanel.add(indirizzoField);
        formPanel.add(new JLabel("Prezzo Totale:"));           formPanel.add(prezzoField);
        formPanel.add(btnCert);        formPanel.add(labelCert);
        formPanel.add(btnFoto);        formPanel.add(labelFoto);
        formPanel.add(btnToggleForm);  formPanel.add(btnInvia);
        add(formPanel, BorderLayout.CENTER);
    }

    private void wireEvents() {
        // Toggle form visibility
        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Pacchetto");
            revalidate(); repaint();
        });

        // Selezione prodotti
        btnSelProd.addActionListener(e -> {
            prodottiSel.clear();
            for (int r : tabellaProdotti.getSelectedRows()) {
                prodottiSel.add(controller.getProdottiMarketplace().get(r));
            }
            JOptionPane.showMessageDialog(
                    this,
                    prodottiSel.size() + " prodotto/i selezionato/i.",
                    "Selezione Prodotti",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        // File pickers
        btnCert.addActionListener(e -> pickFiles(true));
        btnFoto.addActionListener(e -> pickFiles(false));

        // Invia pacchetto
        btnInvia.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(
                    this,
                    "Inviare il pacchetto al curatore per approvazione?",
                    "Conferma invio",
                    JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION) {
                sendToController();
            }
        });
    }

    private void pickFiles(boolean certificati) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        List<File> target = certificati ? certSel : fotoSel;
        target.clear();
        for (File f : chooser.getSelectedFiles()) target.add(f);
        (certificati ? labelCert : labelFoto)
                .setText(target.size() + " file selezionati");
    }

    private void sendToController() {
        PacchettoDto dto = new PacchettoDto(
                nomeField.getText().trim(),
                descrField.getText().trim(),
                indirizzoField.getText().trim(),
                prezzoField.getText().trim(),
                prodottiSel.stream()
                        .map(Prodotto::getNome)
                        .toList(),
                certSel,
                fotoSel
        );

        BiConsumer<Boolean,String> callback = (ok, msg) -> SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this, msg,
                    ok ? "Successo" : "Errore",
                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
            );
            if (ok) {
                resetForm();
                refreshPacchetti();
            }
        });

        controller.inviaPacchetto(dto, callback);
    }

    private void refreshProdotti() {
        modelProdotti.setRowCount(0);
        for (Prodotto p : controller.getProdottiMarketplace()) {
            modelProdotti.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(),
                    p.getQuantita(), p.getPrezzo(), p.getIndirizzo()
            });
        }
    }

    private void refreshPacchetti() {
        modelPacchetti.setRowCount(0);
        for (Pacchetto p : controller.getPacchettiCreatiDaMe()) {
            modelPacchetti.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(), p.getIndirizzo(),
                    p.getPrezzoTotale(),
                    p.getProdotti().size() + " prodotti",
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato(), p.getCommento()
            });
        }
    }

    @Override
    public void notifica(Pacchetto p, String evento) {
        // ignoro il primo “NUOVO_PACCHETTO”
        if ("NUOVO_PACCHETTO".equals(evento)) {
            return;
        }
        // mostro messaggi solo per APPROVATO o RIFIUTATO
        if (!p.getCreatoDa().equalsIgnoreCase(utente.getUsername())) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if ("APPROVATO".equals(evento)) {
                JOptionPane.showMessageDialog(
                        this,
                        "✔ Il tuo pacchetto \"" + p.getNome() + "\" è stato APPROVATO!",
                        "Pacchetto approvato",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            else if ("RIFIUTATO".equals(evento)) {
                String msg = "❌ Il tuo pacchetto \"" + p.getNome() + "\" è stato RIFIUTATO.";
                if (p.getCommento() != null && !p.getCommento().isBlank()) {
                    msg += "\nCommento: " + p.getCommento();
                }
                JOptionPane.showMessageDialog(
                        this,
                        msg,
                        "Pacchetto rifiutato",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            // in ogni caso ricarico la tabella
            refreshPacchetti();
        });
    }


    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManagerPacchetto.rimuoviOsservatore(this);
    }

    private void resetForm() {
        nomeField.setText("");
        descrField.setText("");
        indirizzoField.setText("");
        prezzoField.setText("");
        certSel.clear();
        fotoSel.clear();
        prodottiSel.clear();
        labelCert.setText("Nessun file selezionato");
        labelFoto.setText("Nessun file selezionato");
    }
}
