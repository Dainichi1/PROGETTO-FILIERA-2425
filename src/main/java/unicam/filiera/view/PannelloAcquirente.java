package unicam.filiera.view;

import unicam.filiera.controller.AcquirenteController;
import unicam.filiera.controller.EliminazioneProfiloController;
import unicam.filiera.controller.MappaController;
import unicam.filiera.controller.ObserverManagerItem;
import unicam.filiera.dto.AcquistoItemDto;
import unicam.filiera.dto.AcquistoListaDto;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.EliminazioneProfiloNotifier;
import unicam.filiera.model.observer.OsservatoreEliminazioneProfilo;
import unicam.filiera.model.observer.OsservatoreItem;
import unicam.filiera.dto.PostSocialDto;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class PannelloAcquirente extends JPanel implements OsservatoreItem, OsservatoreEliminazioneProfilo {

    private final AcquirenteController ctrl;
    private final EliminazioneProfiloController eliminaController;
    private final UtenteAutenticato utente;


    private DefaultTableModel modelAcquisti;
    private JTable tabAcquisti;
    private JButton btnRecensione;

    private DefaultTableModel modelAcquistoItems;
    private JTable tabAcquistoItems;

    private JDialog dlgAcquisti;   // per riuso

    private final JButton btnShowSocial = new JButton("Visualizza Social Network");
    private final JButton btnShowPurchases = new JButton("Visualizza acquisti");

    // Marketplace
    private final JTable tabMarketplace;
    private final DefaultTableModel modelMarketplace;
    private final List<Item> itemList = new ArrayList<>();


    // Carrello
    private final JTable tabCarrello;
    private final DefaultTableModel modelCarrello;
    private final JLabel lblTotali = new JLabel("Totale: 0 articoli - €0.00");
    private final JButton btnAcquista = new JButton("Acquista");

    // Fondi
    private final JTextField txtFondi = new JTextField(6);
    private final JButton btnAggiornaFondi = new JButton("Aggiorna Fondi");

    // Controlli in alto
    private final JButton btnShowMarket = new JButton("Visualizza Marketplace");
    private final JButton btnShowCart = new JButton("Visualizza Carrello");
    private final JButton btnShowMap = new JButton("Visualizza Mappa");


    private final JButton btnFiereDisponibili = new JButton("Visualizza Fiere disponibili");
    private final JTable tabFiere;
    private final DefaultTableModel modelFiere;
    private final JButton btnVisualizzaPrenotazioni = new JButton("Visualizza prenotazioni fiere");
    private final JButton btnEliminaProfilo = new JButton("Elimina profilo");

    private final DefaultTableModel modelPrenotazioni = new DefaultTableModel(
            new Object[]{"ID", "Descrizione fiera", "Data", "Persone", "Elimina"}, 0);
    private final JTable tabPrenotazioni = new JTable(modelPrenotazioni);

    public PannelloAcquirente(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;
        this.ctrl = new AcquirenteController(this, utente);
        this.eliminaController = new EliminazioneProfiloController(utente.getUsername());


        if (utente instanceof Acquirente a) {
            txtFondi.setText(String.format("%.2f", a.getFondi()));
        }

        // --- Marketplace table ---
        modelMarketplace = new DefaultTableModel(
                new Object[]{"Seleziona", "Tipo", "Nome", "Descrizione", "Prezzo", "Disponibile", "Quantità", "Certificati", "Foto", "Fasi Produzione", "Azione"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 6;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return (c == 0) ? Boolean.class :
                        (c == 6) ? Integer.class : String.class;
            }
        };


        modelFiere = new DefaultTableModel(
                new Object[]{"Descrizione", "Indirizzo", "Data Inizio", "Data Fine", "Prezzo", "Min. Partecipanti", "Organizzatore"},
                0
        );
        tabFiere = new JTable(modelFiere);

        tabMarketplace = new JTable(modelMarketplace);
        modelMarketplace.addTableModelListener(e -> {
            int col = e.getColumn(), row = e.getFirstRow();
            if (col == 0 && row >= 0) {
                boolean sel = (Boolean) modelMarketplace.getValueAt(row, 0);
                modelMarketplace.setValueAt(sel ? "Aggiungi al carrello" : "", row, 9);
            }
        });

        // --- Carrello table ---
        modelCarrello = new DefaultTableModel(
                new Object[]{"Tipo", "Nome", "Quantità", "Prezzo Unitario", "Totale", "Aggiorna", "Elimina"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return (c == 2) ? Integer.class : String.class;
            }
        };
        tabCarrello = new JTable(modelCarrello);

        // --- Azioni bottoni ---
        btnShowMarket.addActionListener(e -> ctrl.visualizzaMarketplace());
        btnShowCart.addActionListener(e -> ctrl.visualizzaCarrello());
        btnAcquista.addActionListener(e -> mostraDialogPagamento());
        btnFiereDisponibili.addActionListener(e -> ctrl.visualizzaFiereDisponibili());
        btnVisualizzaPrenotazioni.addActionListener(e -> ctrl.visualizzaPrenotazioniFiere());
        btnEliminaProfilo.addActionListener(e -> mostraDialogEliminaProfilo());
        btnShowPurchases.addActionListener(e -> ctrl.visualizzaAcquisti());
        btnShowSocial.addActionListener(e -> ctrl.visualizzaSocialNetwork());
        btnShowMap.addActionListener(e -> {
            MappaController mappaCtrl = new MappaController();
            mappaCtrl.mostra();
        });


        btnAggiornaFondi.addActionListener(e -> {
            try {
                double nuoviFondi = Double.parseDouble(txtFondi.getText().replace(',', '.'));
                ctrl.aggiornaFondiAcquirente(nuoviFondi, (msg, ok) -> {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, msg,
                                ok ? "Successo" : "Errore",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                        if (ok) {
                            txtFondi.setText(String.format("%.2f", nuoviFondi));
                        }
                    });
                });
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Importo non valido!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        ObserverManagerItem.registraOsservatore(this);
        EliminazioneProfiloNotifier.getInstance()
                .subscribe(utente.getUsername(), this);

        initUI();
        aggiungiListenerMarketplace();
        aggiungiListenerCarrello();
    }

    private void initUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnShowMarket);
        top.add(btnFiereDisponibili);
        top.add(btnVisualizzaPrenotazioni);
        top.add(btnEliminaProfilo);
        top.add(btnShowPurchases);
        top.add(btnShowSocial);
        top.add(btnShowCart);
        top.add(btnShowMap);
        top.add(new JLabel("   "));
        top.add(new JLabel("Fondi:"));
        top.add(txtFondi);
        top.add(btnAggiornaFondi);
        top.add(new JLabel("   "));
        top.add(lblTotali);
        top.add(btnAcquista);
        add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabMarketplace),
                new JScrollPane(tabCarrello)
        );
        split.setDividerLocation(300);
        add(split, BorderLayout.CENTER);
        btnAcquista.setEnabled(false); // Disabilita di default all’avvio
    }

    /**
     * Chiamato dal controller per popolare marketplace
     */
    public void showMarketplace(List<Object> lista) {
        modelMarketplace.setRowCount(0);
        itemList.clear();
        for (Object o : lista) {
            if (o instanceof Item item) {
                itemList.add(item);
                String tipo;
                double prezzo;
                Object disp;
                String certificati = "", foto = "";
                String fasiProduzione = "";

                if (item instanceof ProdottoTrasformato pt) {
                    tipo = "Prodotto Trasformato";
                    prezzo = pt.getPrezzo();
                    disp = pt.getQuantita();
                    certificati = String.join(", ", pt.getCertificati());
                    foto = String.join(", ", pt.getFoto());
                    // Mostra le fasi come: "Descrizione (ProduttoreUsername, ProdottoOrigine)"
                    fasiProduzione = pt.getFasiProduzione().stream()
                            .map(f -> f.getDescrizioneFase() +
                                    " (" + f.getProduttoreUsername() +
                                    (f.getProdottoOrigine() != null && !f.getProdottoOrigine().isBlank()
                                            ? ", " + f.getProdottoOrigine() : "") +
                                    ")")
                            .reduce((a, b) -> a + " → " + b).orElse("-");
                } else if (item instanceof Prodotto p) {
                    tipo = "Prodotto";
                    prezzo = p.getPrezzo();
                    disp = p.getQuantita();
                    certificati = String.join(", ", p.getCertificati());
                    foto = String.join(", ", p.getFoto());
                } else if (item instanceof Pacchetto pac) {
                    tipo = "Pacchetto";
                    prezzo = pac.getPrezzoTotale();
                    disp = pac.getQuantita();
                    certificati = String.join(", ", pac.getCertificati());
                    foto = String.join(", ", pac.getFoto());
                } else {
                    tipo = "";
                    prezzo = 0;
                    disp = "";
                }

                modelMarketplace.addRow(new Object[]{
                        false, tipo, item.getNome(), item.getDescrizione(),
                        prezzo, disp, 0,
                        certificati,
                        foto,
                        fasiProduzione, // colonna con le fasi
                        ""
                });
            }
        }
        btnAcquista.setEnabled(false);
    }


    /**
     * Chiamato dal controller per popolare il carrello
     */
    public void showCart(List<CartItemDto> items, CartTotalsDto tot) {
        modelCarrello.setRowCount(0);
        for (CartItemDto dto : items) {
            modelCarrello.addRow(new Object[]{
                    dto.getTipo(),
                    dto.getNome(),
                    dto.getQuantita(),
                    dto.getPrezzoUnitario(),
                    dto.getTotale(),
                    "Aggiorna",
                    "Elimina"
            });
        }
        lblTotali.setText(
                String.format("Totale: %d articoli - €%.2f",
                        tot.getTotaleArticoli(),
                        tot.getCostoTotale())
        );
        btnAcquista.setEnabled(!items.isEmpty());
    }


    public void showAcquistiDialog(List<AcquistoListaDto> lista) {
        // crea dialog se non esiste
        if (dlgAcquisti == null) {
            dlgAcquisti = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "I miei acquisti", Dialog.ModalityType.APPLICATION_MODAL);
            dlgAcquisti.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dlgAcquisti.setSize(900, 520);
            dlgAcquisti.setLocationRelativeTo(this);

            // master
            modelAcquisti = new DefaultTableModel(
                    new Object[]{"ID", "Data/Ora", "Totale", "Stato", "Metodo", "Elenco (preview)"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            tabAcquisti = new JTable(modelAcquisti);

            // dettaglio
            modelAcquistoItems = new DefaultTableModel(
                    new Object[]{"Nome", "Tipo", "Quantità", "Prezzo Unit.", "Totale"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            tabAcquistoItems = new JTable(modelAcquistoItems);

            JSplitPane split = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(tabAcquisti),
                    new JScrollPane(tabAcquistoItems)
            );
            split.setDividerLocation(260);

            // === Bottom bar con bottone Recensione ===
            btnRecensione = new JButton("Lascia recensione");
            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomBar.add(btnRecensione);

            // Contenitore principale
            JPanel content = new JPanel(new BorderLayout());
            content.add(split, BorderLayout.CENTER);
            content.add(bottomBar, BorderLayout.SOUTH);
            dlgAcquisti.setContentPane(content);

            // Selezione riga master → carica dettaglio
            tabAcquisti.getSelectionModel().addListSelectionListener(ev -> {
                if (ev.getValueIsAdjusting()) return;
                int row = tabAcquisti.getSelectedRow();
                if (row < 0) return;
                int id = (int) modelAcquisti.getValueAt(row, 0);
                ctrl.caricaDettaglioAcquisto(id);
            });

            // === Listener del bottone Recensione ===
            btnRecensione.addActionListener(e -> {
                int rigaAcq = tabAcquisti.getSelectedRow();
                if (rigaAcq == -1) {
                    JOptionPane.showMessageDialog(dlgAcquisti,
                            "Seleziona un acquisto (tabella in alto).",
                            "Attenzione", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int rigaItem = tabAcquistoItems.getSelectedRow();
                if (rigaItem == -1) {
                    JOptionPane.showMessageDialog(dlgAcquisti,
                            "Seleziona l'item acquistato (tabella in basso).",
                            "Attenzione", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idAcquisto = (int) modelAcquisti.getValueAt(rigaAcq, 0);
                String nomeItem = (String) modelAcquistoItems.getValueAt(rigaItem, 0);
                String tipoItem = (String) modelAcquistoItems.getValueAt(rigaItem, 1);

                FormRecensioneDialog dialog = new FormRecensioneDialog(
                        SwingUtilities.getWindowAncestor(this),
                        idAcquisto,                // se il costruttore vuole long, fai: (long) idAcquisto
                        nomeItem,
                        tipoItem,
                        ctrl.getUsername()
                );
                dialog.setVisible(true);

                if (dialog.isConfermato()) {
                    ctrl.lasciaRecensione(dialog.getPostSocialDto());
                }
            });
        }

        // popola master
        modelAcquisti.setRowCount(0);
        for (AcquistoListaDto a : lista) {
            modelAcquisti.addRow(new Object[]{
                    a.getId(),
                    a.getDataOra(),
                    a.getTotale(),
                    a.getStatoPagamento(),
                    a.getTipoMetodoPagamento(),
                    a.getElencoItem()
            });
        }

        // pulisci dettaglio all’apertura
        modelAcquistoItems.setRowCount(0);

        dlgAcquisti.setVisible(true);
    }

    public void updateDettaglioAcquisto(List<AcquistoItemDto> items) {
        if (modelAcquistoItems == null) return;
        modelAcquistoItems.setRowCount(0);
        for (AcquistoItemDto it : items) {
            modelAcquistoItems.addRow(new Object[]{
                    it.getNomeItem(),
                    it.getTipoItem(),
                    it.getQuantita(),
                    it.getPrezzoUnitario(),
                    it.getTotale()
            });
        }
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

        // Popola
        if (posts != null && !posts.isEmpty()) {
            for (PostSocialDto p : posts) {
                model.addRow(new Object[]{
                        p.getCreatedAt(),
                        p.getAutoreUsername(),
                        p.getTipoItem(),
                        p.getNomeItem(),
                        p.getTitolo(),
                        p.getTesto(),

                });
            }
        }

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Feed globale dei post pubblicati sui social"));
        dlg.add(north, BorderLayout.NORTH);
        dlg.add(new JScrollPane(tab), BorderLayout.CENTER);

        if (posts == null || posts.isEmpty()) {
            dlg.add(new JLabel("Nessun contenuto da mostrare.", SwingConstants.CENTER), BorderLayout.SOUTH);
        }

        dlg.setVisible(true);
    }


    /**
     * Callback factory per mostrare popup di esito
     */
    private BiConsumer<String, Boolean> showResult() {
        return (msg, ok) -> SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this, msg,
                    ok ? "Successo" : "Errore",
                    ok ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.ERROR_MESSAGE
            );
        });
    }

    /**
     * Validazione lato UI e invio addToCart()
     */
    private void aggiungiListenerMarketplace() {
        tabMarketplace.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tabMarketplace.rowAtPoint(e.getPoint());
                int col = tabMarketplace.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return;

                // Click su "Aggiungi al carrello"
                if (col == 9) {
                    String az = (String) modelMarketplace.getValueAt(row, 9);
                    if (!"Aggiungi al carrello".equals(az)) return;

                    Item item = itemList.get(row);
                    Object qObj = modelMarketplace.getValueAt(row, 6);
                    int q = (qObj instanceof Integer) ? (Integer) qObj : 0;

                    ctrl.addToCart(item, q, showResult());
                    return;
                }

                // Click su "Certificati"
                if (col == 7) { // Certificati
                    Item item = itemList.get(row);
                    List<String> certs =
                            (item instanceof Prodotto p) ? p.getCertificati()
                                    : (item instanceof Pacchetto pac) ? pac.getCertificati()
                                    : (item instanceof ProdottoTrasformato pt) ? pt.getCertificati()
                                    : List.of();
                    mostraDialogFile("Certificati", certs, "uploads/certificati");
                    return;
                }

                if (col == 8) { // Foto
                    Item item = itemList.get(row);
                    List<String> fotos =
                            (item instanceof Prodotto p) ? p.getFoto()
                                    : (item instanceof Pacchetto pac) ? pac.getFoto()
                                    : (item instanceof ProdottoTrasformato pt) ? pt.getFoto()
                                    : List.of();
                    mostraDialogFile("Foto", fotos, "uploads/foto");
                    return;
                }
            }
        });
    }

    private void mostraDialogFile(String titolo, List<String> files, String cartellaBase) {
        if (files == null || files.isEmpty() || (files.size() == 1 && files.get(0).isBlank())) {
            JOptionPane.showMessageDialog(this, "Nessun file disponibile.", titolo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JList<String> lista = new JList<>(files.toArray(new String[0]));
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int res = JOptionPane.showConfirmDialog(
                this, new JScrollPane(lista),
                titolo + " (clicca su un file e poi OK per aprirlo)",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res == JOptionPane.OK_OPTION) {
            String selected = lista.getSelectedValue();
            if (selected != null && !selected.isBlank()) {
                try {
                    java.io.File f = new java.io.File(cartellaBase, selected);
                    if (!f.exists()) {
                        JOptionPane.showMessageDialog(this, "File non trovato:\n" + f.getAbsolutePath(), "Errore", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    java.awt.Desktop.getDesktop().open(f);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Impossibile aprire il file:\n" + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Listener per “Aggiorna” e “Elimina” nel carrello
     */
    private void aggiungiListenerCarrello() {
        tabCarrello.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = tabCarrello.rowAtPoint(e.getPoint());
                int viewCol = tabCarrello.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewCol < 0) return;

                int modelRow = tabCarrello.convertRowIndexToModel(viewRow);
                int modelCol = tabCarrello.convertColumnIndexToModel(viewCol);

                Object cellValue = modelCarrello.getValueAt(modelRow, modelCol);
                String nome = (String) modelCarrello.getValueAt(modelRow, 1);

                if ("Elimina".equals(cellValue)) {
                    ctrl.requestDeleteCartItem(nome, showResult());
                } else if ("Aggiorna".equals(cellValue)) {
                    Object qObj = modelCarrello.getValueAt(modelRow, 2);
                    int q = (qObj instanceof Integer) ? (Integer) qObj : 0;
                    if (q == 0) {
                        ctrl.requestDeleteCartItem(nome, showResult());
                    } else {
                        ctrl.updateCartItem(nome, q, showResult());
                    }
                }
            }
        });
    }

    public void avvisaErrore(String msg) {
        JOptionPane.showMessageDialog(
                this, msg, "Errore", JOptionPane.ERROR_MESSAGE
        );
    }

    public void aggiornaFondi(double nuoviFondi) {
        txtFondi.setText(String.format("%.2f", nuoviFondi));
    }

    private void mostraDialogPagamento() {
        String[] metodi = {"Carta", "Paypal", "Pagamento alla consegna"};
        TipoMetodoPagamento[] tipi = {
                TipoMetodoPagamento.CARTA_DI_CREDITO,
                TipoMetodoPagamento.BONIFICO,
                TipoMetodoPagamento.PAGAMENTO_ALLA_CONSEGNA
        };

        JComboBox<String> combo = new JComboBox<>(metodi);
        int res = JOptionPane.showConfirmDialog(
                this,
                combo,
                "Seleziona metodo di pagamento",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (res != JOptionPane.OK_OPTION) return;

        TipoMetodoPagamento selezionato = tipi[combo.getSelectedIndex()];
        mostraDialogConfermaAcquisto(selezionato);
    }

    private void mostraDialogConfermaAcquisto(TipoMetodoPagamento metodo) {
        int res = JOptionPane.showConfirmDialog(
                this,
                "Vuoi procedere con l'acquisto?",
                "Conferma acquisto",
                JOptionPane.YES_NO_OPTION
        );
        if (res == JOptionPane.YES_OPTION) {
            effettuaAcquistoConEsiti(metodo);
        }
    }

    private void effettuaAcquistoConEsiti(TipoMetodoPagamento metodo) {
        ctrl.effettuaAcquisto(metodo, (msg, ok) -> {
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
                    ctrl.visualizzaCarrello();
                } else {
                    int retry = JOptionPane.showOptionDialog(
                            this,
                            msg + "\nVuoi riprovare con un altro metodo di pagamento?",
                            "Pagamento fallito",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE,
                            null,
                            new Object[]{"Sì, scegli altro metodo", "No, annulla acquisto"},
                            "Sì, scegli altro metodo"
                    );
                    if (retry == JOptionPane.YES_OPTION) {
                        mostraDialogPagamento();
                    }
                }
            });
        });
    }

    @Override
    public void notificaItem(String nomeItem, String evento) {
        SwingUtilities.invokeLater(ctrl::visualizzaMarketplace);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManagerItem.rimuoviOsservatore(this);
        // unsubscribe dall’osservatore eliminazione profilo
        EliminazioneProfiloNotifier.getInstance()
                .unsubscribe(utente.getUsername(), this);
    }


    public void showFiereDisponibili(List<Fiera> fiere) {
        modelFiere.setRowCount(0);
        for (Fiera f : fiere) {
            modelFiere.addRow(new Object[]{
                    f.getDescrizione(),
                    f.getIndirizzo(),
                    f.getDataInizio(),
                    f.getDataFine(),
                    f.getPrezzo(),
                    f.getNumeroMinPartecipanti(),
                    f.getOrganizzatore()
            });
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tabFiere), BorderLayout.CENTER);

        JButton btnPrenota = new JButton("Prenota ingresso");
        panel.add(btnPrenota, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Fiere disponibili", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);

        btnPrenota.addActionListener(e -> {
            int selectedRow = tabFiere.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Seleziona una fiera.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Fiera fieraSelezionata = fiere.get(selectedRow);

            String input = JOptionPane.showInputDialog(dialog, "Inserisci il numero di persone:");
            if (input == null) return;
            int numeroPersone;
            try {
                numeroPersone = Integer.parseInt(input);
                if (numeroPersone <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Numero di persone non valido.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ctrl.prenotaIngressoFiera(fieraSelezionata.getId(), numeroPersone, (msg, ok) -> {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(dialog, msg, ok ? "Successo" : "Errore",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE)
                );
            });
        });

        dialog.setVisible(true);
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


    public void showPrenotazioniFiere(List<PrenotazioneFiera> prenotazioni, List<Fiera> tutteLeFiere) {
        modelPrenotazioni.setRowCount(0);
        for (PrenotazioneFiera p : prenotazioni) {
            String desc = tutteLeFiere.stream()
                    .filter(f -> f.getId() == p.getIdFiera())
                    .findFirst().map(Fiera::getDescrizione).orElse("?");
            modelPrenotazioni.addRow(new Object[]{
                    p.getId(),
                    desc,
                    p.getDataPrenotazione(),
                    p.getNumeroPersone(),
                    "Elimina"
            });
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Prenotazioni fiere", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new JScrollPane(tabPrenotazioni));
        dialog.setSize(700, 300);
        dialog.setLocationRelativeTo(this);

        tabPrenotazioni.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tabPrenotazioni.rowAtPoint(e.getPoint());
                int col = tabPrenotazioni.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) { // Colonna "Elimina"
                    long idPren = Long.parseLong(modelPrenotazioni.getValueAt(row, 0).toString());
                    int conferma = JOptionPane.showConfirmDialog(dialog,
                            "Sei sicuro di voler eliminare la prenotazione?",
                            "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
                    if (conferma == JOptionPane.YES_OPTION) {
                        ctrl.eliminaPrenotazioneFiera(idPren, (msg, ok) -> {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, msg,
                                    ok ? "Successo" : "Errore",
                                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
                            if (ok) {
                                modelPrenotazioni.removeRow(row);
                            }
                        });
                    }
                }
            }
        });

        dialog.setVisible(true);
    }
}
