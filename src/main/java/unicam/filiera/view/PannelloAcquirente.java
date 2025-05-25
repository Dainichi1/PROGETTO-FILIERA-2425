package unicam.filiera.view;

import unicam.filiera.controller.AcquirenteController;
import unicam.filiera.controller.EliminazioneProfiloController;
import unicam.filiera.controller.ObserverManagerItem;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.OsservatoreItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class PannelloAcquirente extends JPanel implements OsservatoreItem {
    private final AcquirenteController ctrl;
    private final EliminazioneProfiloController eliminaController;


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

        top.add(btnShowCart);
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
