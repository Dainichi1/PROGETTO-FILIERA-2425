package unicam.filiera.view;

import unicam.filiera.controller.DistributoreController;
import unicam.filiera.controller.ObserverManagerPacchetto;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.observer.OsservatorePacchetto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * View pura: mostra l’interfaccia e delega la logica
 * a {@link DistributoreController}.
 */
public class PannelloDistributore extends JPanel implements OsservatorePacchetto {
    private final UtenteAutenticato utente;
    private final DistributoreController controller;
    private boolean editMode = false;
    private String originalName;

    private final List<File> certSel = new ArrayList<>();
    private final List<File> fotoSel = new ArrayList<>();
    private final List<Prodotto> prodottiSel = new ArrayList<>();

    private final JTextField nomeField = new JTextField();
    private final JTextField descrField = new JTextField();
    private final JTextField indirizzoField = new JTextField();
    private final JTextField prezzoField = new JTextField();

    private final JLabel labelCert = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Chiudi form");
    private final JButton btnSelProd = new JButton("Seleziona Prodotti");
    private final JButton btnCert = new JButton("Seleziona Certificati");
    private final JButton btnFoto = new JButton("Seleziona Foto");
    private final JButton btnInvia = new JButton("Invia Pacchetto");

    private final DefaultTableModel modelProdotti;
    private final JTable tabellaProdotti;
    private final DefaultTableModel modelPacchetti;
    private final JTable tabellaPacchetti;

    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private boolean formVisibile = true;

    public PannelloDistributore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;
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
        String[] colProd = {"Nome", "Descrizione", "Quantità", "Prezzo", "Indirizzo"};
        modelProdotti = new DefaultTableModel(colProd, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // nessuna cella è editabile via GUI
                return false;
            }
        };
        tabellaProdotti = new JTable(modelProdotti);

        // Pacchetti table
        String[] colPack = {
                "Nome", "Descrizione", "Indirizzo", "Prezzo Totale",
                "Prodotti", "Certificati", "Foto", "Stato", "Commento",
                "Elimina", "Modifica"
        };


        modelPacchetti = new DefaultTableModel(colPack, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // nessuna cella è editabile via GUI (ma restano intercettabili con il mouse)
                return false;
            }
        };
        tabellaPacchetti = new JTable(modelPacchetti);

        tabellaPacchetti.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tabellaPacchetti.rowAtPoint(e.getPoint());
                int col = tabellaPacchetti.columnAtPoint(e.getPoint());
                if (row < 0) return;

                String nome = (String) modelPacchetti.getValueAt(row, 0);
                String stato = modelPacchetti.getValueAt(row, 7).toString();

                // ——— Elimina (colonna 9) ——————————————————————
                if (col == 9) {
                    if (!stato.equals("IN_ATTESA") && !stato.equals("RIFIUTATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloDistributore.this,
                                "Puoi eliminare solo pacchetti in attesa o rifiutati.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    int conferma = JOptionPane.showConfirmDialog(
                            PannelloDistributore.this,
                            "Vuoi davvero eliminare il pacchetto \"" + nome + "\"?",
                            "Conferma eliminazione",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (conferma == JOptionPane.YES_OPTION) {
                        boolean ok = controller.eliminaPacchetto(nome);
                        if (ok) {
                            JOptionPane.showMessageDialog(
                                    PannelloDistributore.this,
                                    "Pacchetto eliminato con successo.",
                                    "Successo",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshPacchetti();
                        } else {
                            JOptionPane.showMessageDialog(
                                    PannelloDistributore.this,
                                    "Errore durante l'eliminazione.",
                                    "Errore",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                    return;
                }

                // ——— Modifica (colonna 10) ——————————————————————
                if (col == 10) {
                    if (!stato.equals("RIFIUTATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloDistributore.this,
                                "Puoi modificare solo pacchetti RIFIUTATI.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    // Recupera il Pacchetto dal controller
                    Pacchetto p = controller.trovaPacchettoPerNome(nome);
                    if (p == null) {
                        JOptionPane.showMessageDialog(
                                PannelloDistributore.this,
                                "Impossibile recuperare il pacchetto per la modifica.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    // Entra in modalità edit
                    enterEditMode(p);
                }
            }
        });


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

    private void enterEditMode(Pacchetto p) {
        editMode = true;
        originalName = p.getNome();
        // apri form se chiuso
        if (!formVisibile) btnToggleForm.doClick();
        // popola i campi
        nomeField.setText(p.getNome());
        descrField.setText(p.getDescrizione());
        indirizzoField.setText(p.getIndirizzo());
        prezzoField.setText(String.valueOf(p.getPrezzoTotale()));
        prodottiSel.clear();
        prodottiSel.addAll(p.getProdotti());
        labelCert.setText("Ricarica certificati");
        labelFoto.setText("Ricarica foto");
        btnInvia.setText("Aggiorna Pacchetto");
        btnToggleForm.setText("Annulla modifica");
    }

    private void exitEditMode() {
        editMode = false;
        originalName = null;
        resetForm();
        btnInvia.setText("Invia Pacchetto");
        btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Pacchetto");
    }

    private void buildForm() {
        formPanel.add(new JLabel("Nome Pacchetto:"));
        formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrizione:"));
        formPanel.add(descrField);
        formPanel.add(new JLabel("Indirizzo luogo vendita:"));
        formPanel.add(indirizzoField);
        formPanel.add(new JLabel("Prezzo Totale:"));
        formPanel.add(prezzoField);
        formPanel.add(btnCert);
        formPanel.add(labelCert);
        formPanel.add(btnFoto);
        formPanel.add(labelFoto);
        formPanel.add(btnToggleForm);
        formPanel.add(btnInvia);
        add(formPanel, BorderLayout.CENTER);
    }

    private void wireEvents() {
        // Toggle form visibility
        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Pacchetto");
            revalidate();
            repaint();
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
                    editMode
                            ? "Sei sicuro di voler aggiornare e rinviare il pacchetto?"
                            : "Inviare il pacchetto al curatore per approvazione?",
                    editMode ? "Conferma aggiornamento" : "Conferma invio",
                    JOptionPane.YES_NO_OPTION
            ) != JOptionPane.YES_OPTION) {
                if (editMode) exitEditMode();
                return;
            }

            // raccogli i dati...
            Map<String, String> datiInput = Map.of(
                    "nome", nomeField.getText().trim(),
                    "descrizione", descrField.getText().trim(),
                    "indirizzo", indirizzoField.getText().trim(),
                    "prezzo", prezzoField.getText().trim()
            );
            List<String> nomiProdotti = prodottiSel.stream()
                    .map(Prodotto::getNome).toList();

            BiConsumer<Boolean, String> callback = (ok, msg) -> SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        this, msg,
                        ok ? "Successo" : "Errore",
                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                );
                if (ok) {
                    if (editMode) exitEditMode();
                    resetForm();
                    refreshPacchetti();
                }
            });

            if (editMode) {
                controller.gestisciModificaPacchetto(
                        originalName,
                        datiInput,
                        nomiProdotti,
                        List.copyOf(certSel),
                        List.copyOf(fotoSel),
                        callback
                );
            } else {
                controller.gestisciInvioPacchetto(
                        datiInput,
                        nomiProdotti,
                        List.copyOf(certSel),
                        List.copyOf(fotoSel),
                        callback
                );
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
        // 1. Raccogli dati grezzi dai campi di input
        var datiInput = Map.of(
                "nome", nomeField.getText().trim(),
                "descrizione", descrField.getText().trim(),
                "indirizzo", indirizzoField.getText().trim(),
                "prezzo", prezzoField.getText().trim()
        );

        // 2. Raccogli i nomi dei prodotti selezionati
        List<String> nomiProdotti = prodottiSel.stream()
                .map(Prodotto::getNome)
                .toList();

        // 3. Definisci il callback
        BiConsumer<Boolean, String> callback = (ok, msg) -> SwingUtilities.invokeLater(() -> {
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

        // 4. Chiama il controller
        controller.gestisciInvioPacchetto(
                datiInput,
                nomiProdotti,
                List.copyOf(certSel),
                List.copyOf(fotoSel),
                callback
        );
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
                    p.getNome(),
                    p.getDescrizione(),
                    p.getIndirizzo(),
                    p.getPrezzoTotale(),
                    p.getProdotti().size() + " prodotti",
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato(),
                    p.getCommento(),
                    "Elimina",                                    // colonna Azioni 1
                    p.getStato() == StatoProdotto.RIFIUTATO       // colonna Azioni 2
                            ? "Modifica"
                            : ""
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
            } else if ("RIFIUTATO".equals(evento)) {
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
