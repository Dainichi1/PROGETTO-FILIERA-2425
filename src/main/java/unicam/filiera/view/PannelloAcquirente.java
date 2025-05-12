package unicam.filiera.view;

import unicam.filiera.controller.AcquirenteController;
import unicam.filiera.model.*;
import unicam.filiera.util.ValidatoreAcquisto;
import unicam.filiera.util.ValidatoreMarketplace;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PannelloAcquirente extends JPanel {

    private final AcquirenteController ctrl;
    private final JTable tabMarketplace;
    private final JTable tabCarrello;

    private final DefaultTableModel modelMarketplace;
    private final DefaultTableModel modelCarrello;
    private final JLabel lblFondi = new JLabel();
    private final List<Item> itemList = new ArrayList<>();

    private final JButton btnShowMarket;
    private final JButton btnShowCart;
    private final JTextField txtFondi = new JTextField(6);
    private final JButton btnAggiornaFondi = new JButton("Aggiorna Fondi");

    public PannelloAcquirente(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.ctrl = new AcquirenteController(this, utente);
        if (utente instanceof Acquirente acquirente) {
            txtFondi.setText(String.format("%.2f", acquirente.getFondi()));
        }

        modelMarketplace = new DefaultTableModel(
                new Object[]{"Seleziona", "Tipo", "Nome", "Descrizione", "Prezzo", "Disponibile", "Quantità", "Azione"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Boolean.class;
                    case 6 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        tabMarketplace = new JTable(modelMarketplace);

        modelCarrello = new DefaultTableModel(
                new Object[]{"Tipo", "Nome", "Quantità", "Prezzo"}, 0);
        tabCarrello = new JTable(modelCarrello);

        btnShowMarket = new JButton("Visualizza Marketplace");
        btnShowMarket.addActionListener(e -> ctrl.visualizzaMarketplace());

        btnShowCart = new JButton("Visualizza Carrello");
        btnShowCart.addActionListener(e -> aggiornaCarrello());

        btnAggiornaFondi.addActionListener(e -> {
            try {
                double nuoviFondi = Double.parseDouble(txtFondi.getText().trim());
                ctrl.aggiornaFondiAcquirente(nuoviFondi);
                avvisaSuccesso("Fondi aggiornati con successo.");
            } catch (NumberFormatException ex) {
                avvisaErrore("Inserisci un valore numerico valido.");
            }
        });

        initUI();
        aggiungiValidatoreQuantita();
        aggiungiListenerCheckboxEAzione();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(btnShowMarket);
        topPanel.add(btnShowCart);
        topPanel.add(new JLabel("Fondi disponibili:"));
        topPanel.add(txtFondi);
        topPanel.add(btnAggiornaFondi);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabMarketplace),
                new JScrollPane(tabCarrello)
        );
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);
    }

    public void aggiornaCampoFondi(double fondi) {
        txtFondi.setText(String.format("%.2f", fondi));
    }

    public void showMarketplace(List<Object> lista) {
        modelMarketplace.setRowCount(0);
        itemList.clear();

        for (Object o : lista) {
            if (o instanceof Item item) {
                itemList.add(item);
                String tipo = item instanceof Prodotto ? "Prodotto" : "Pacchetto";
                double prezzo = (item instanceof Prodotto p) ? p.getPrezzo() : ((Pacchetto) item).getPrezzoTotale();
                Object disponibile = (item instanceof Prodotto p) ? p.getQuantita() : "-";

                modelMarketplace.addRow(new Object[]{
                        false, tipo, item.getNome(), item.getDescrizione(),
                        prezzo, disponibile, 0, ""
                });
            }
        }
    }

    private void aggiornaCarrello() {
        modelCarrello.setRowCount(0);
        for (int i = 0; i < modelMarketplace.getRowCount(); i++) {
            Boolean selezionato = (Boolean) modelMarketplace.getValueAt(i, 0);
            if (Boolean.TRUE.equals(selezionato)) {
                String tipo = (String) modelMarketplace.getValueAt(i, 1);
                String nome = (String) modelMarketplace.getValueAt(i, 2);
                Object prezzoObj = modelMarketplace.getValueAt(i, 4);
                Integer quantita = (Integer) modelMarketplace.getValueAt(i, 6);
                if (quantita != null && quantita > 0) {
                    double prezzoUnitario = (prezzoObj instanceof Number) ? ((Number) prezzoObj).doubleValue() : 0.0;
                    double prezzoTotale = prezzoUnitario * quantita;
                    modelCarrello.addRow(new Object[]{tipo, nome, quantita, prezzoTotale});
                }
            }
        }
    }

    private void aggiungiValidatoreQuantita() {
        modelMarketplace.addTableModelListener(new TableModelListener() {
            private boolean aggiornamentoInterno = false;
            @Override
            public void tableChanged(TableModelEvent e) {
                if (aggiornamentoInterno) return;
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) {
                    int row = e.getFirstRow();
                    try {
                        String tipo = (String) modelMarketplace.getValueAt(row, 1);
                        Object disponibileObj = modelMarketplace.getValueAt(row, 5);
                        Object quantitaObj = modelMarketplace.getValueAt(row, 6);
                        int richiesta = quantitaObj instanceof Integer ? (Integer) quantitaObj : 0;
                        if (tipo.equals("Prodotto")) {
                            int disponibile = disponibileObj instanceof Integer ? (Integer) disponibileObj : 0;
                            ValidatoreAcquisto.validaQuantita(richiesta, disponibile);
                        } else if (tipo.equals("Pacchetto")) {
                            if (richiesta <= 0) throw new IllegalArgumentException("⚠ La quantità deve essere almeno 1.");
                        }
                    } catch (IllegalArgumentException ex) {
                        avvisaErrore(ex.getMessage());
                        aggiornamentoInterno = true;
                        modelMarketplace.setValueAt(0, row, 6);
                        aggiornamentoInterno = false;
                    }
                }
            }
        });
    }

    private void aggiungiListenerCheckboxEAzione() {
        tabMarketplace.getModel().addTableModelListener(e -> {
            int col = e.getColumn();
            int row = e.getFirstRow();
            if (col == 0 && row >= 0) {
                Boolean selezionato = (Boolean) modelMarketplace.getValueAt(row, 0);
                modelMarketplace.setValueAt(selezionato ? "Aggiungi al carrello" : "", row, 7);
            }
        });

        tabMarketplace.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabMarketplace.rowAtPoint(evt.getPoint());
                int col = tabMarketplace.columnAtPoint(evt.getPoint());
                if (col == 7 && row >= 0) {
                    String azione = (String) modelMarketplace.getValueAt(row, 7);
                    if ("Aggiungi al carrello".equals(azione)) {
                        int quantita = (Integer) modelMarketplace.getValueAt(row, 6);
                        try {
                            Item item = itemList.get(row);
                            ValidatoreMarketplace.validaTipo(item instanceof Prodotto ? "Prodotto" : "Pacchetto");
                            ValidatoreMarketplace.validaNome(item.getNome());
                            if (item instanceof Prodotto p) {
                                ValidatoreAcquisto.validaQuantita(quantita, p.getQuantita());
                            } else if (quantita <= 0) {
                                throw new IllegalArgumentException("⚠ La quantità deve essere almeno 1.");
                            }
                            ctrl.aggiungiAlCarrello(item, quantita);
                            avvisaSuccesso(item.getNome() + " aggiunto al carrello.");
                            aggiornaCarrello();
                        } catch (IllegalArgumentException ex) {
                            avvisaErrore(ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    public void avvisaSuccesso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void avvisaErrore(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    public void showCarrello(List<Object[]> carrello) {
        modelCarrello.setRowCount(0);
        for (Object[] riga : carrello) {
            modelCarrello.addRow(riga);
        }
    }
}
