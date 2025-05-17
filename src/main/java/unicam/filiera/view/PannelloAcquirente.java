package unicam.filiera.view;

import unicam.filiera.controller.AcquirenteController;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
import unicam.filiera.model.Item;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Acquirente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class PannelloAcquirente extends JPanel {
    private final AcquirenteController ctrl;

    // Marketplace
    private final JTable tabMarketplace;
    private final DefaultTableModel modelMarketplace;
    private final List<Item> itemList = new ArrayList<>();

    // Carrello
    private final JTable tabCarrello;
    private final DefaultTableModel modelCarrello;
    private final JLabel lblTotali = new JLabel("Totale: 0 articoli - €0.00");

    // Fondi
    private final JTextField txtFondi = new JTextField(6);
    private final JButton btnAggiornaFondi = new JButton("Aggiorna Fondi");

    // Controlli in alto
    private final JButton btnShowMarket = new JButton("Visualizza Marketplace");
    private final JButton btnShowCart = new JButton("Visualizza Carrello");

    public PannelloAcquirente(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.ctrl = new AcquirenteController(this, utente);

        // Imposta campo fondi iniziale
        if (utente instanceof Acquirente a) {
            txtFondi.setText(String.format("%.2f", a.getFondi()));
        }

        // --- Marketplace table ---
        modelMarketplace = new DefaultTableModel(
                new Object[]{"Seleziona", "Tipo", "Nome", "Descrizione", "Prezzo", "Disponibile", "Quantità", "Azione"},
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
        tabMarketplace = new JTable(modelMarketplace);
        modelMarketplace.addTableModelListener(e -> {
            int col = e.getColumn(), row = e.getFirstRow();
            if (col == 0 && row >= 0) {
                boolean sel = (Boolean) modelMarketplace.getValueAt(row, 0);
                modelMarketplace.setValueAt(sel ? "Aggiungi al carrello" : "", row, 7);
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
        btnAggiornaFondi.addActionListener(e -> {
            try {
                // Leggi il valore dal campo di testo
                double nuoviFondi = Double.parseDouble(txtFondi.getText().replace(',', '.'));

                // Chiedi al controller di aggiornare i fondi
                ctrl.aggiornaFondiAcquirente(nuoviFondi, (msg, ok) -> {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, msg,
                                ok ? "Successo" : "Errore",
                                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                        if (ok) {
                            // Se l’update è ok, aggiorna il campo testo (per sicurezza)
                            txtFondi.setText(String.format("%.2f", nuoviFondi));
                        }
                    });
                });
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Importo non valido!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });


        initUI();
        aggiungiListenerMarketplace();
        aggiungiListenerCarrello();
    }

    private void initUI() {
        // Top panel
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnShowMarket);
        top.add(btnShowCart);
        top.add(new JLabel("   "));
        top.add(new JLabel("Fondi:"));
        top.add(txtFondi);
        top.add(btnAggiornaFondi);
        top.add(new JLabel("   "));
        top.add(lblTotali);
        add(top, BorderLayout.NORTH);

        // Split pane
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabMarketplace),
                new JScrollPane(tabCarrello)
        );
        split.setDividerLocation(300);
        add(split, BorderLayout.CENTER);
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
                String tipo = (item instanceof Prodotto) ? "Prodotto" : "Pacchetto";
                double prezzo = (item instanceof Prodotto p) ? p.getPrezzo()
                        : ((Pacchetto) item).getPrezzoTotale();
                Object disp = (item instanceof Prodotto p) ? p.getQuantita() : "-";
                modelMarketplace.addRow(new Object[]{
                        false, tipo, item.getNome(), item.getDescrizione(),
                        prezzo, disp, 0, ""
                });
            }
        }
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
                if (row < 0 || col != 7) return;
                String az = (String) modelMarketplace.getValueAt(row, 7);
                if (!"Aggiungi al carrello".equals(az)) return;

                Item item = itemList.get(row);
                Object qObj = modelMarketplace.getValueAt(row, 6);
                int q = (qObj instanceof Integer) ? (Integer) qObj : 0;

                ctrl.addToCart(item, q, showResult());
            }
        });
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

                // Converte in indice del modello, nel caso tu abbia usato sorter/filter
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
                        // se vuole zero, puoi pure chiamare delete
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
}
