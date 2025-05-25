package unicam.filiera.view;

import unicam.filiera.controller.TrasformatoreController;
import unicam.filiera.controller.EliminazioneProfiloController;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.OsservatoreProdottoTrasformato;
import unicam.filiera.model.observer.ProdottoTrasformatoNotifier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PannelloTrasformatore extends JPanel implements OsservatoreProdottoTrasformato {

    private final UtenteAutenticato utente;
    private final TrasformatoreController controller;
    private final EliminazioneProfiloController eliminaController;

    private boolean editMode = false;
    private String originalName;

    // Fasi produzione
    private final DefaultTableModel fasiModel = new DefaultTableModel(
            new Object[]{"Descrizione", "Produttore", "Prodotto base", "Indirizzo"}, 0
    );
    private final JTable tabellaFasi = new JTable(fasiModel);
    private final JButton btnAggiungiFase = new JButton("Aggiungi Fase");
    private final JButton btnRimuoviFase = new JButton("Rimuovi Fase");

    private final List<File> certSel = new ArrayList<>();
    private final List<File> fotoSel = new ArrayList<>();

    private final JTextField nomeField = new JTextField();
    private final JTextField descrField = new JTextField();
    private final JTextField quantField = new JTextField();
    private final JTextField prezzoField = new JTextField();
    private final JTextField indirizzoField = new JTextField();

    private final JLabel labelCert = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Crea Prodotto Trasformato");
    private final JButton btnCert = new JButton("Seleziona certificati");
    private final JButton btnFoto = new JButton("Seleziona foto");
    private final JButton btnInvia = new JButton("Invia Prodotto Trasformato");
    private final JButton btnVisiteDisponibili = new JButton("Visualizza visite disponibili");
    private final JButton btnVisualizzaPrenotazioniVisite = new JButton("Visualizza prenotazioni visite");
    private final JButton btnEliminaProfilo = new JButton("Elimina profilo");

    private final DefaultTableModel model;
    private final JTable tabella;
    private final JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
    private boolean formVisibile = false;

    public PannelloTrasformatore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;
        this.controller = new TrasformatoreController(utente.getUsername());
        this.eliminaController = new EliminazioneProfiloController(utente.getUsername());

        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        add(benv, BorderLayout.NORTH);

        buildForm();
        add(btnToggleForm, BorderLayout.SOUTH);

        btnToggleForm.addActionListener(e -> toggleForm());

        JPanel pannelloVisite = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pannelloVisite.add(btnVisiteDisponibili);
        pannelloVisite.add(btnVisualizzaPrenotazioniVisite);
        add(pannelloVisite, BorderLayout.WEST);

        btnVisiteDisponibili.addActionListener(e -> controller.visualizzaVisiteDisponibili(this));
        btnVisualizzaPrenotazioniVisite.addActionListener(e -> controller.visualizzaPrenotazioniVisite(this));

        // Tabella Prodotti Trasformati
        String[] cols = {
                "Nome", "Descrizione", "Qtà", "Prezzo", "Indirizzo",
                "Certificati", "Foto", "Stato", "Commento", "Fasi produzione",
                "Elimina", "Modifica"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabella = new JTable(model);
        add(new JScrollPane(tabella), BorderLayout.EAST);

        tabella.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tabella.rowAtPoint(e.getPoint());
                int col = tabella.columnAtPoint(e.getPoint());
                if (row < 0) return;

                String nomeProdotto = (String) model.getValueAt(row, 0);
                String statoStr = model.getValueAt(row, 7).toString();

                // Colonna 10: Elimina
                if (col == 10) {
                    if (!statoStr.equals("IN_ATTESA") && !statoStr.equals("RIFIUTATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloTrasformatore.this,
                                "Puoi eliminare solo prodotti in attesa o rifiutati.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    int conferma = JOptionPane.showConfirmDialog(
                            PannelloTrasformatore.this,
                            "Vuoi davvero eliminare il prodotto \"" + nomeProdotto + "\"?",
                            "Conferma eliminazione",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (conferma == JOptionPane.YES_OPTION) {
                        boolean ok = controller.eliminaProdottoTrasformato(nomeProdotto);
                        JOptionPane.showMessageDialog(
                                PannelloTrasformatore.this,
                                ok ? "Prodotto eliminato con successo." : "Errore durante l'eliminazione.",
                                ok ? "Successo" : "Errore",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                        );
                        if (ok) refreshTable();
                    }
                } else if (col == 11) {
                    if (!statoStr.equals("RIFIUTATO")) {
                        JOptionPane.showMessageDialog(
                                PannelloTrasformatore.this,
                                "Puoi modificare solo prodotti RIFIUTATI.",
                                "Operazione non permessa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    ProdottoTrasformato p = controller.trovaProdottoTrasformatoPerNome(nomeProdotto);
                    if (p != null) {
                        enterEditMode(p);
                    } else {
                        JOptionPane.showMessageDialog(
                                PannelloTrasformatore.this,
                                "Errore nel recupero del prodotto per la modifica.",
                                "Errore",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        btnCert.addActionListener(e -> chooseFiles(true));
        btnFoto.addActionListener(e -> chooseFiles(false));

        btnAggiungiFase.addActionListener(e -> {
            JTextField desc = new JTextField();

            List<Produttore> produttori = controller.getProduttoriDisponibili();
            JComboBox<Produttore> selectProduttore = new JComboBox<>(produttori.toArray(new Produttore[0]));
            JComboBox<Prodotto> selectProdottoBase = new JComboBox<>();

            selectProduttore.addActionListener(e2 -> {
                selectProdottoBase.removeAllItems();
                Produttore sel = (Produttore) selectProduttore.getSelectedItem();
                if (sel != null) {
                    List<Prodotto> prodotti = controller.getProdottiApprovatiByProduttore(sel.getUsername());
                    for (Prodotto p : prodotti) {
                        selectProdottoBase.addItem(p);
                    }
                }
            });

            if (selectProduttore.getItemCount() > 0) {
                selectProduttore.setSelectedIndex(0);
                selectProduttore.getActionListeners()[0].actionPerformed(null);
            }

            Object[] fields = {
                    "Descrizione fase:", desc,
                    "Produttore:", selectProduttore,
                    "Prodotto base:", selectProdottoBase
            };
            int ok = JOptionPane.showConfirmDialog(this, fields, "Aggiungi fase", JOptionPane.OK_CANCEL_OPTION);

            if (ok == JOptionPane.OK_OPTION) {
                Produttore prodSelezionato = (Produttore) selectProduttore.getSelectedItem();
                Prodotto prodottoBaseSelezionato = (Prodotto) selectProdottoBase.getSelectedItem();

                if (prodottoBaseSelezionato == null) {
                    JOptionPane.showMessageDialog(this, "Seleziona un prodotto base!", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                fasiModel.addRow(new Object[]{
                        desc.getText(),
                        prodSelezionato != null ? prodSelezionato.getUsername() : "",
                        prodottoBaseSelezionato.getNome(),
                        prodottoBaseSelezionato.getIndirizzo()
                });
            }
        });

        btnRimuoviFase.addActionListener(e -> {
            int row = tabellaFasi.getSelectedRow();
            if (row >= 0) fasiModel.removeRow(row);
        });

        btnInvia.addActionListener(e -> {
            int scelta = JOptionPane.showConfirmDialog(
                    this,
                    editMode
                            ? "Sei sicuro di voler aggiornare e rinviare il prodotto trasformato?"
                            : "Inviare il prodotto trasformato al Curatore per approvazione?",
                    editMode ? "Conferma aggiornamento" : "Conferma invio",
                    JOptionPane.YES_NO_OPTION
            );
            if (scelta != JOptionPane.YES_OPTION) {
                if (editMode) exitEditMode();
                return;
            }

            // Dati base
            Map<String, String> datiInput = Map.of(
                    "nome", nomeField.getText().trim(),
                    "descrizione", descrField.getText().trim(),
                    "quantita", quantField.getText().trim(),
                    "prezzo", prezzoField.getText().trim(),
                    "indirizzo", indirizzoField.getText().trim()
            );

            // Recupero le fasi di produzione dalla tabella
            List<ProdottoTrasformatoDto.FaseProduzioneDto> fasi = new ArrayList<>();
            for (int i = 0; i < fasiModel.getRowCount(); i++) {
                String desc = (String) fasiModel.getValueAt(i, 0);
                String prod = (String) fasiModel.getValueAt(i, 1);
                String base = (String) fasiModel.getValueAt(i, 2);
                fasi.add(new ProdottoTrasformatoDto.FaseProduzioneDto(desc, prod, base));
            }

            if (editMode) {
                controller.gestisciModificaProdottoTrasformato(
                        originalName,
                        datiInput,
                        List.copyOf(certSel),
                        List.copyOf(fotoSel),
                        fasi,
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
                controller.gestisciInvioProdottoTrasformato(
                        datiInput,
                        List.copyOf(certSel),
                        List.copyOf(fotoSel),
                        fasi,
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

        // Bottone elimina profilo in basso a destra
        btnEliminaProfilo.addActionListener(e -> mostraDialogEliminaProfilo());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnEliminaProfilo);
        add(bottomPanel, BorderLayout.PAGE_END);

        refreshTable();
        ProdottoTrasformatoNotifier.getInstance().registraOsservatore(this);
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

        // Fasi di produzione: tabella + bottoni
        formPanel.add(new JLabel("Fasi di produzione:"));
        JPanel fasiPanel = new JPanel(new BorderLayout());
        fasiPanel.add(new JScrollPane(tabellaFasi), BorderLayout.CENTER);
        JPanel fasiBtnPanel = new JPanel();
        fasiBtnPanel.add(btnAggiungiFase);
        fasiBtnPanel.add(btnRimuoviFase);
        fasiPanel.add(fasiBtnPanel, BorderLayout.SOUTH);
        formPanel.add(fasiPanel);

        formPanel.add(new JLabel());
        formPanel.add(btnInvia);
        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);
    }

    private void enterEditMode(ProdottoTrasformato p) {
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
        btnInvia.setText("Aggiorna Prodotto Trasformato");
        btnToggleForm.setText("Annulla modifica");

        // Popola anche le fasi produzione
        fasiModel.setRowCount(0);
        if (p.getFasiProduzione() != null) {
            for (FaseProduzione f : p.getFasiProduzione()) {
                fasiModel.addRow(new Object[]{f.getDescrizioneFase(), f.getProduttoreUsername(), f.getProdottoOrigine()});
            }
        }

        revalidate();
        repaint();
    }

    private void exitEditMode() {
        editMode = false;
        originalName = null;
        resetForm();
        formVisibile = false;
        formPanel.setVisible(false);
        btnInvia.setText("Invia Prodotto Trasformato");
        btnToggleForm.setText("Crea Prodotto Trasformato");
        revalidate();
        repaint();
    }

    private void toggleForm() {
        if (editMode) {
            exitEditMode();
        } else {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto Trasformato");
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
        fasiModel.setRowCount(0);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (ProdottoTrasformato p : controller.getProdottiTrasformatiCreatiDaMe()) {
            model.addRow(new Object[]{
                    p.getNome(), p.getDescrizione(), p.getQuantita(), p.getPrezzo(), p.getIndirizzo(),
                    String.join(", ", p.getCertificati()), String.join(", ", p.getFoto()),
                    p.getStato(), p.getCommento(),
                    p.fasiProduzioneAsString(),
                    "Elimina",
                    p.getStato() == StatoProdotto.RIFIUTATO ? "Modifica" : ""
            });
        }
    }

    // notifica prodotto trasformato
    @Override
    public void notifica(ProdottoTrasformato prod, String evento) {
        if (!"APPROVATO".equals(evento) && !"RIFIUTATO".equals(evento)) return;
        if (!prod.getCreatoDa().equalsIgnoreCase(utente.getUsername())) return;

        SwingUtilities.invokeLater(() -> {
            boolean approved = "APPROVATO".equals(evento);
            String title = approved ? "Prodotto trasformato approvato" : "Prodotto trasformato rifiutato";
            int type = approved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            String msg = approved
                    ? "✔ Il tuo prodotto trasformato \"" + prod.getNome() + "\" è stato APPROVATO!"
                    : "❌ Il tuo prodotto trasformato \"" + prod.getNome() + "\" è stato RIFIUTATO."
                      + (prod.getCommento() != null && !prod.getCommento().isBlank() ? "\nCommento: " + prod.getCommento() : "");
            JOptionPane.showMessageDialog(this, msg, title, type);
            refreshTable();
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ProdottoTrasformatoNotifier.getInstance().rimuoviOsservatore(this);
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
        DefaultTableModel modelPren = new DefaultTableModel(
                new Object[]{"ID", "Descrizione visita", "Data prenotazione", "Persone", "Elimina"}, 0);
        JTable tabPrenotazioni = new JTable(modelPren);
        for (PrenotazioneVisita p : prenotazioni) {
            String desc = tutteLeVisite.stream()
                    .filter(v -> v.getId() == p.getIdVisita())
                    .findFirst().map(VisitaInvito::getDescrizione).orElse("?");
            modelPren.addRow(new Object[]{
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
                    long idPren = Long.parseLong(modelPren.getValueAt(row, 0).toString());
                    int conferma = JOptionPane.showConfirmDialog(dialog,
                            "Sei sicuro di voler eliminare la prenotazione?",
                            "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
                    if (conferma == JOptionPane.YES_OPTION) {
                        controller.eliminaPrenotazioneVisita(idPren, (msg, ok) -> {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, msg,
                                    ok ? "Successo" : "Errore",
                                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
                            if (ok) {
                                modelPren.removeRow(row);
                            }
                        });
                    }
                }
            }
        });

        dialog.setVisible(true);
    }
}
