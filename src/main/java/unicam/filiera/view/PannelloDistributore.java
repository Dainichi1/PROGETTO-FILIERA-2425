package unicam.filiera.view;

import unicam.filiera.controller.DistributoreController;
import unicam.filiera.controller.EliminazioneProfiloController;
import unicam.filiera.controller.MappaController;
import unicam.filiera.controller.ObserverManagerPacchetto;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.EliminazioneProfiloNotifier;
import unicam.filiera.model.observer.OsservatoreEliminazioneProfilo;
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
public class PannelloDistributore extends JPanel implements OsservatorePacchetto, OsservatoreEliminazioneProfilo {
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
    private final JTextField quantitaField = new JTextField();


    private final JLabel labelCert = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Chiudi form");
    private final JButton btnSelProd = new JButton("Seleziona Prodotti");
    private final JButton btnCert = new JButton("Seleziona Certificati");
    private final JButton btnFoto = new JButton("Seleziona Foto");
    private final JButton btnInvia = new JButton("Invia Pacchetto");
    private final JButton btnVisiteDisponibili = new JButton("Visualizza visite disponibili");
    private final JButton btnVisualizzaPrenotazioniVisite = new JButton("Visualizza prenotazioni visite");
    private final JButton btnShowSocial = new JButton("Visualizza Social Network");
    private final JButton btnShowMap = new JButton("Visualizza Mappa");

    private final EliminazioneProfiloController eliminaController;
    private final JButton btnEliminaProfilo = new JButton("Elimina profilo");
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
        this.eliminaController = new EliminazioneProfiloController(utente.getUsername());

        // Header
        JLabel benvenuto = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER
        );
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        JPanel pannelloVisite = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pannelloVisite.add(btnVisiteDisponibili);
        pannelloVisite.add(btnVisualizzaPrenotazioniVisite);
        pannelloVisite.add(btnShowSocial);
        add(pannelloVisite, BorderLayout.WEST);

        btnVisiteDisponibili.addActionListener(e -> controller.visualizzaVisiteDisponibili(this));
        btnVisualizzaPrenotazioniVisite.addActionListener(e -> controller.visualizzaPrenotazioniVisite(this));
        btnShowSocial.addActionListener(e -> {
            try {
                var posts = controller.getSocialFeed();
                showSocialNetworkDialog(posts);  // metodo della view (sotto)
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
        btnShowMap.addActionListener(e -> {
            MappaController mappaCtrl = new MappaController();
            mappaCtrl.mostra();
        });


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
                "Nome", "Descrizione", "Indirizzo", "Prezzo Totale", "Quantità",
                "Prodotti", "Certificati", "Foto", "Stato", "Commento",
                "Elimina", "Modifica", "Social"
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
                String stato = modelPacchetti.getValueAt(row, 8).toString(); // enum -> "APPROVATO", "RIFIUTATO", ...

                // ——— Elimina (colonna 10) ——————————————————————
                if (col == 10) {
                    if (!"IN_ATTESA".equals(stato) && !"RIFIUTATO".equals(stato)) {
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
                        JOptionPane.showMessageDialog(
                                PannelloDistributore.this,
                                ok ? "Pacchetto eliminato con successo." : "Errore durante l'eliminazione.",
                                ok ? "Successo" : "Errore",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        );
                        if (ok) refreshPacchetti();
                    }
                    return;
                }

                // ——— Modifica (colonna 11) ——————————————————————
                if (col == 11) {
                    if (!"RIFIUTATO".equals(stato)) {
                        JOptionPane.showMessageDialog(
                                PannelloDistributore.this,
                                "Puoi modificare solo pacchetti RIFIUTATI.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
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
                    enterEditMode(p);
                    return;
                }

                // ——— Pubblica annuncio su Social (colonna 12) ————————————————
                if (col == 12) {
                    if (!"APPROVATO".equals(stato)) {
                        JOptionPane.showMessageDialog(
                                PannelloDistributore.this,
                                "Il pulsante è disponibile solo per pacchetti APPROVATI.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // 1) finestra titolo/testo
                    FormAnnuncioItemDialog dlg = new FormAnnuncioItemDialog(
                            SwingUtilities.getWindowAncestor(PannelloDistributore.this),
                            nome
                    );
                    dlg.setVisible(true);
                    if (!dlg.isConfermato()) return;

                    // 2) conferma finale
                    int choice = JOptionPane.showConfirmDialog(
                            PannelloDistributore.this,
                            "Sei sicuro di voler pubblicare l’annuncio sul Social?",
                            "Conferma pubblicazione",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice != JOptionPane.YES_OPTION) return;

                    // 3) pubblicazione reale tramite controller
                    // dentro if (col == 12)
                    controller.pubblicaAnnuncioPacchetto(
                            nome,
                            dlg.getTitolo(),
                            dlg.getTesto(),
                            (msg, ok) -> SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(
                                            PannelloDistributore.this, msg,
                                            ok ? "Successo" : "Errore",
                                            ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                                    )
                            )
                    );

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
        EliminazioneProfiloNotifier.getInstance()
                .subscribe(utente.getUsername(), this);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnEliminaProfilo);
        bottomPanel.add(btnShowMap);
        add(bottomPanel, BorderLayout.SOUTH);

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
        quantitaField.setText(String.valueOf(p.getQuantita()));
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
        formPanel.add(new JLabel("Quantità:"));
        formPanel.add(quantitaField);

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
                    "prezzo", prezzoField.getText().trim(),
                    "quantita", quantitaField.getText().trim()
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
                "prezzo", prezzoField.getText().trim(),
                "quantita", quantitaField.getText().trim()
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

    public void showSocialNetworkDialog(List<PostSocialDto> posts) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Social Network", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setSize(900, 520);
        dlg.setLocationRelativeTo(this);

        String[] cols = {"Data/Ora", "Autore", "Tipo", "Nome Item", "Titolo", "Testo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tab = new JTable(model);

        if (posts != null && !posts.isEmpty()) {
            for (PostSocialDto p : posts) {
                model.addRow(new Object[]{
                        p.getCreatedAt(),
                        p.getAutoreUsername(),
                        p.getTipoItem(),
                        p.getNomeItem(),
                        p.getTitolo(),
                        p.getTesto()
                });
            }
        }

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Feed globale dei post pubblicati sui social"));
        dlg.add(north, BorderLayout.NORTH);
        dlg.add(new JScrollPane(tab), BorderLayout.CENTER);

        if (posts == null || posts.isEmpty()) {
            dlg.add(new JLabel("Nessun contenuto da mostrare.", SwingConstants.CENTER),
                    BorderLayout.SOUTH);
        }

        dlg.setVisible(true);
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
            boolean approvato = p.getStato() == StatoProdotto.APPROVATO;
            modelPacchetti.addRow(new Object[]{
                    p.getNome(),
                    p.getDescrizione(),
                    p.getIndirizzo(),
                    p.getPrezzoTotale(),
                    p.getQuantita(),
                    p.getProdotti().size() + " prodotti",
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato(),
                    p.getCommento(),
                    "Elimina",                                    // colonna Azioni 1
                    p.getStato() == StatoProdotto.RIFIUTATO       // colonna Azioni 2
                            ? "Modifica"
                            : "",
                    p.getStato() == StatoProdotto.APPROVATO ? "Pubblica su Social" : ""
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
        EliminazioneProfiloNotifier.getInstance()
                .unsubscribe(utente.getUsername(), this);
    }

    // ==== OsservatoreEliminazioneProfilo ====
    @Override
    public void onRichiestaRifiutata(String username, int richiestaId, String motivo) {
        if (!username.equalsIgnoreCase(utente.getUsername())) return;
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        this,
                        "La tua richiesta di eliminazione (ID " + richiestaId + ") è stata RIFIUTATA.\n",
                        "Richiesta rifiutata",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );
    }

    @Override
    public void onProfiloEliminato(String username, int richiestaId) {
        if (!username.equalsIgnoreCase(utente.getUsername())) return;

        SwingUtilities.invokeLater(() -> {
            String msg = "Il tuo profilo è stato eliminato (richiesta ID " + richiestaId + ").\n"
                    + "Verrai riportato alla schermata iniziale...";
            // Nessun bottone: si chiude da solo dopo 3s e fa logout
            showAutoCloseInfoAndThen("Profilo eliminato", msg, 3000, this::logoutToHome);
        });
    }

    /**
     * Mostra un info dialog senza bottoni che si chiude da solo dopo 'millis' e poi esegue 'afterClose'.
     */
    private void showAutoCloseInfoAndThen(String title, String message, int millis, Runnable afterClose) {
        JOptionPane pane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{},
                null
        );
        JDialog dialog = pane.createDialog(SwingUtilities.getWindowAncestor(this), title);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);

        Timer t = new Timer(millis, e -> {
            dialog.dispose();
            if (afterClose != null) afterClose.run();
        });
        t.setRepeats(false);
        t.start();

        dialog.setVisible(true);
    }

    /**
     * Ritorna alla home (riutilizza MainWindow esistente se c'è).
     */
    private void logoutToHome() {
        SwingUtilities.invokeLater(() -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainWindow mw) {
                mw.tornaAllaHome();
            } else {
                if (w != null) w.dispose();
                MainWindow mw2 = new MainWindow();
                mw2.setVisible(true);
            }
        });
    }


    private void resetForm() {
        nomeField.setText("");
        descrField.setText("");
        indirizzoField.setText("");
        prezzoField.setText("");
        quantitaField.setText("");
        certSel.clear();
        fotoSel.clear();
        prodottiSel.clear();
        labelCert.setText("Nessun file selezionato");
        labelFoto.setText("Nessun file selezionato");
    }

    public void showVisiteDisponibili(List<VisitaInvito> visite) {
        if (visite == null || visite.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna visita disponibile.", "Visite disponibili", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] opzioni = new String[visite.size()];
        for (int i = 0; i < visite.size(); i++) {
            VisitaInvito v = visite.get(i);
            opzioni[i] = String.format(
                    "<html><b>%s</b> | %s → %s | Organizzatore: %s | Destinatari: %s</html>",
                    v.getDescrizione(),
                    v.getDataInizio(),
                    v.getDataFine(),
                    v.getOrganizzatore(),
                    String.join(", ", v.getDestinatari())
            );
        }

        int scelta = JOptionPane.showOptionDialog(
                this,
                "Seleziona una visita da prenotare:",
                "Visite disponibili",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opzioni,
                opzioni[0]
        );

        if (scelta >= 0) {
            VisitaInvito visitaSelezionata = visite.get(scelta);
            String numeroStr = JOptionPane.showInputDialog(this, "Quante persone vuoi prenotare?", "1");
            if (numeroStr != null && !numeroStr.isBlank()) {
                try {
                    int numeroPersone = Integer.parseInt(numeroStr.trim());
                    controller.prenotaVisita(
                            visitaSelezionata.getId(),
                            numeroPersone,
                            (msg, ok) -> SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(this, msg, ok ? "Successo" : "Errore",
                                        ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                            })
                    );
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Numero persone non valido.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void showPrenotazioniVisite(List<PrenotazioneVisita> prenotazioni, List<VisitaInvito> tutteLeVisite) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Descrizione visita", "Data prenotazione", "Persone", "Elimina"}, 0);
        JTable tabPrenotazioni = new JTable(model);

        for (PrenotazioneVisita p : prenotazioni) {
            String desc = tutteLeVisite.stream()
                    .filter(v -> v.getId() == p.getIdVisita())
                    .findFirst().map(VisitaInvito::getDescrizione).orElse("?");
            model.addRow(new Object[]{
                    p.getId(),
                    desc,
                    p.getDataPrenotazione(),
                    p.getNumeroPersone(),
                    "Elimina"
            });
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Prenotazioni visite", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new JScrollPane(tabPrenotazioni));
        dialog.setSize(700, 300);
        dialog.setLocationRelativeTo(this);

        tabPrenotazioni.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tabPrenotazioni.rowAtPoint(e.getPoint());
                int col = tabPrenotazioni.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) { // Colonna "Elimina"
                    long idPren = Long.parseLong(model.getValueAt(row, 0).toString());
                    int conferma = JOptionPane.showConfirmDialog(dialog,
                            "Sei sicuro di voler eliminare la prenotazione?",
                            "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
                    if (conferma == JOptionPane.YES_OPTION) {
                        controller.eliminaPrenotazioneVisita(idPren, (msg, ok) -> {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, msg,
                                    ok ? "Successo" : "Errore",
                                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
                            if (ok) {
                                model.removeRow(row);
                            }
                        });
                    }
                }
            }
        });

        dialog.setVisible(true);
    }


}
