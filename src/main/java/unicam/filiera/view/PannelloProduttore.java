package unicam.filiera.view;

import unicam.filiera.controller.ProduttoreController;
import unicam.filiera.controller.EliminazioneProfiloController;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PannelloProduttore extends JPanel implements OsservatoreProdotto {

    private final UtenteAutenticato utente;
    private final ProduttoreController controller;
    private final EliminazioneProfiloController eliminaController;
    private boolean editMode = false;
    private String originalName;

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
    private final JButton btnVisiteDisponibili = new JButton("Visualizza visite disponibili");
    private final JButton btnVisualizzaPrenotazioniVisite = new JButton("Visualizza prenotazioni visite");
    private final JButton btnEliminaProfilo = new JButton("Elimina profilo");
    private final JButton btnShowSocial = new JButton("Visualizza Social Network");

    private final DefaultTableModel model;
    private final JTable tabella;
    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private boolean formVisibile = false;

    public PannelloProduttore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;
        this.controller = new ProduttoreController(utente.getUsername());
        this.eliminaController = new EliminazioneProfiloController(utente.getUsername());

        // Header
        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        add(benv, BorderLayout.NORTH);

        buildForm();
        add(btnToggleForm, BorderLayout.SOUTH);

        btnToggleForm.addActionListener(e -> toggleForm());

        // Pannello visite
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

        // Table
        String[] cols = {
                "Nome", "Descrizione", "Qtà", "Prezzo", "Indirizzo",
                "Certificati", "Foto", "Stato", "Commento",
                "Elimina", "Modifica", "Social"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // mai editabile direttamente
                return false;
            }
        };
        tabella = new JTable(model);

        add(new JScrollPane(tabella), BorderLayout.EAST);

        // MouseListener tabella
        tabella.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tabella.rowAtPoint(e.getPoint());
                int col = tabella.columnAtPoint(e.getPoint());
                if (row < 0) return;

                String nomeProdotto = (String) model.getValueAt(row, 0);
                String statoStr = String.valueOf(model.getValueAt(row, 7)); // enum -> "APPROVATO", "RIFIUTATO", ...

                // COLONNA 9: Elimina
                if (col == 9) {
                    if (!statoStr.equals("IN_ATTESA") && !statoStr.equals("RIFIUTATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloProduttore.this,
                                "Puoi eliminare solo prodotti in attesa o rifiutati.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    int conferma = JOptionPane.showConfirmDialog(
                            PannelloProduttore.this,
                            "Vuoi davvero eliminare il prodotto \"" + nomeProdotto + "\"?",
                            "Conferma eliminazione",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (conferma == JOptionPane.YES_OPTION) {
                        boolean ok = controller.eliminaProdotto(nomeProdotto);
                        JOptionPane.showMessageDialog(
                                PannelloProduttore.this,
                                ok ? "Prodotto eliminato con successo." : "Errore durante l'eliminazione.",
                                ok ? "Successo" : "Errore",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        );
                        if (ok) refreshTable();
                    }

                    // COLONNA 10: Modifica
                } else if (col == 10) {
                    if (!statoStr.equals("RIFIUTATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloProduttore.this,
                                "Puoi modificare solo prodotti RIFIUTATI.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    Prodotto p = controller.trovaProdottoPerNome(nomeProdotto);
                    if (p != null) {
                        enterEditMode(p);
                    } else {
                        JOptionPane.showMessageDialog(
                                PannelloProduttore.this,
                                "Errore nel recupero del prodotto per la modifica.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }

                    // COLONNA 11: Pubblica annuncio su Social (solo APPROVATI)
                } else if (col == 11) {
                    if (!statoStr.equals("APPROVATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloProduttore.this,
                                "Il pulsante è disponibile solo per prodotti APPROVATI.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // 1) finestra titolo/testo
                    FormAnnuncioItemDialog dlg = new FormAnnuncioItemDialog(
                            SwingUtilities.getWindowAncestor(PannelloProduttore.this),
                            nomeProdotto
                    );
                    dlg.setVisible(true);
                    if (!dlg.isConfermato()) return;

                    // 2) conferma finale
                    int choice = JOptionPane.showConfirmDialog(
                            PannelloProduttore.this,
                            "Sei sicuro di voler pubblicare l’annuncio sul Social?",
                            "Conferma pubblicazione",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice != JOptionPane.YES_OPTION) return;

                    // 3) pubblicazione reale tramite controller
                    controller.pubblicaAnnuncioItem(
                            nomeProdotto,
                            dlg.getTitolo(),
                            dlg.getTesto(),
                            (msg, ok) -> SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(
                                            PannelloProduttore.this, msg,
                                            ok ? "Successo" : "Errore",
                                            ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                                    )
                            )
                    );
                }
            }
        });


        // File selectors
        btnCert.addActionListener(e -> chooseFiles(true));
        btnFoto.addActionListener(e -> chooseFiles(false));

        // Invio dati
        btnInvia.addActionListener(e -> {
            int scelta = JOptionPane.showConfirmDialog(
                    this,
                    editMode
                            ? "Sei sicuro di voler aggiornare e rinviare il prodotto al Curatore?"
                            : "Inviare il prodotto al Curatore per approvazione?",
                    editMode ? "Conferma aggiornamento" : "Conferma invio",
                    JOptionPane.YES_NO_OPTION
            );
            if (scelta != JOptionPane.YES_OPTION) {
                if (editMode) exitEditMode();
                return;
            }

            Map<String, String> datiInput = Map.of(
                    "nome", nomeField.getText().trim(),
                    "descrizione", descrField.getText().trim(),
                    "quantita", quantField.getText().trim(),
                    "prezzo", prezzoField.getText().trim(),
                    "indirizzo", indirizzoField.getText().trim()
            );

            if (editMode) {
                controller.gestisciModificaProdotto(
                        originalName,
                        datiInput,
                        List.copyOf(certSel),
                        List.copyOf(fotoSel),
                        (successo, msg) -> SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this, msg,
                                    successo ? "Successo" : "Errore",
                                    successo ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                            );
                            if (successo) {
                                exitEditMode();
                                refreshTable();
                            }
                        })
                );
            } else {
                controller.gestisciInvioProdotto(
                        datiInput,
                        List.copyOf(certSel),
                        List.copyOf(fotoSel),
                        (successo, msg) -> SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this, msg,
                                    successo ? "Successo" : "Errore",
                                    successo ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                            );
                            if (successo) {
                                resetForm();
                                refreshTable();
                            }
                        })
                );
            }
        });

        // BOTTONE ELIMINA PROFILO in basso a destra
        btnEliminaProfilo.addActionListener(e -> mostraDialogEliminaProfilo());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnEliminaProfilo);
        add(bottomPanel, BorderLayout.PAGE_END);

        refreshTable();
        ProdottoNotifier.getInstance().registraOsservatore(this);
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

    private void buildForm() {
        formPanel.add(new JLabel("Nome prodotto:"));
        formPanel.add(nomeField);
        formPanel.add(new JLabel("Descrizione:"));
        formPanel.add(descrField);
        formPanel.add(new JLabel("Quantità:"));
        formPanel.add(quantField);
        formPanel.add(new JLabel("Prezzo:"));
        formPanel.add(prezzoField);
        formPanel.add(new JLabel("Indirizzo:"));
        formPanel.add(indirizzoField);
        formPanel.add(btnCert);
        formPanel.add(labelCert);
        formPanel.add(btnFoto);
        formPanel.add(labelFoto);
        formPanel.add(new JLabel());
        formPanel.add(btnInvia);
        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);
    }

    private void enterEditMode(Prodotto p) {
        editMode = true;
        originalName = p.getNome();
        formVisibile = true;
        formPanel.setVisible(true);
        nomeField.setText(p.getNome());
        descrField.setText(p.getDescrizione());
        quantField.setText(String.valueOf(p.getQuantita()));
        prezzoField.setText(String.valueOf(p.getPrezzo()));
        indirizzoField.setText(p.getIndirizzo());
        certSel.clear();
        fotoSel.clear();
        labelCert.setText("Ricarica certificati");
        labelFoto.setText("Ricarica foto");
        btnInvia.setText("Aggiorna Prodotto");
        btnToggleForm.setText("Annulla modifica");
        revalidate();
        repaint();
    }


    private void exitEditMode() {
        editMode = false;
        originalName = null;
        resetForm();
        formVisibile = false;
        formPanel.setVisible(false);
        btnInvia.setText("Invia Prodotto");
        btnToggleForm.setText("Crea Prodotto");
        revalidate();
        repaint();
    }


    private void toggleForm() {
        if (editMode) {
            // In modalità modifica, il bottone è "Annulla modifica"
            exitEditMode();
        } else {
            // Toggle normale
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto");
            revalidate();
            repaint();
        }
    }


    private void chooseFiles(boolean cert) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        if (cert) {
            certSel.clear();
            certSel.addAll(List.of(chooser.getSelectedFiles()));
            labelCert.setText(certSel.size() + " file selezionati");
        } else {
            fotoSel.clear();
            fotoSel.addAll(List.of(chooser.getSelectedFiles()));
            labelFoto.setText(fotoSel.size() + " file selezionati");
        }
    }

    private void resetForm() {
        nomeField.setText("");
        descrField.setText("");
        quantField.setText("");
        prezzoField.setText("");
        indirizzoField.setText("");
        certSel.clear();
        fotoSel.clear();
        labelCert.setText("Nessun file selezionato");
        labelFoto.setText("Nessun file selezionato");
    }

    private void refreshTable() {
        // dentro refreshTable()
        model.setRowCount(0);
        for (Prodotto p : controller.getProdottiCreatiDaMe()) {
            boolean approvato = p.getStato() == StatoProdotto.APPROVATO;
            model.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(), p.getQuantita(), p.getPrezzo(), p.getIndirizzo(),
                    String.join(", ", p.getCertificati()), String.join(", ", p.getFoto()),
                    p.getStato(), p.getCommento(),
                    "Elimina",
                    p.getStato() == StatoProdotto.RIFIUTATO ? "Modifica" : "",
                    p.getStato() == StatoProdotto.APPROVATO ? "Pubblica su Social" : ""  // col. 11
            });

        }

    }


    @Override
    public void notifica(Prodotto prod, String evento) {
        if (!"APPROVATO".equals(evento) && !"RIFIUTATO".equals(evento)) return;
        if (!prod.getCreatoDa().equalsIgnoreCase(utente.getUsername())) return;

        SwingUtilities.invokeLater(() -> {
            boolean approved = "APPROVATO".equals(evento);
            String title = approved ? "Prodotto approvato" : "Prodotto rifiutato";
            int type = approved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            String msg = approved
                    ? "✔ Il tuo prodotto \"" + prod.getNome() + "\" è stato APPROVATO!"
                    : "❌ Il tuo prodotto \"" + prod.getNome() + "\" è stato RIFIUTATO."
                    + (prod.getCommento() != null && !prod.getCommento().isBlank() ? "\nCommento: " + prod.getCommento() : "");
            JOptionPane.showMessageDialog(this, msg, title, type);
            refreshTable();
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ProdottoNotifier.getInstance().rimuoviOsservatore(this);
    }

    public void showVisiteDisponibili(List<VisitaInvito> visite) {
        if (visite == null || visite.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna visita disponibile.", "Visite disponibili", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Costruisci una lista di descrizioni e salvale per la selezione
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

        // Selettore per l’utente
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

        // Se l’utente seleziona una visita
        if (scelta >= 0) {
            VisitaInvito visitaSelezionata = visite.get(scelta);
            // Chiedi il numero di persone
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

    // metodo per mostrare la tabella e gestire la cancellazione:
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
