package unicam.filiera.view;

import unicam.filiera.controller.AcquirenteController;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.util.ValidatoreAcquisto;
import unicam.filiera.util.ValidatoreMarketplace;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PannelloAcquirente extends JPanel {

    private final AcquirenteController ctrl;
    private final JTable tabMarketplace;
    private final JTable tabCarrello;
    private final DefaultTableModel modelMarketplace;
    private final DefaultTableModel modelCarrello;

    private final JButton btnShowMarket;
    private final JButton btnShowCart;

    public PannelloAcquirente(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.ctrl = new AcquirenteController(this, utente);

        // --- MODELLO MARKETPLACE ---
        modelMarketplace = new DefaultTableModel(
                new Object[]{"Seleziona", "Tipo", "Nome", "Descrizione", "Prezzo", "Disponibile", "Quantità", "Azione"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6; // Solo Checkbox e Quantità
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

        // --- MODELLO CARRELLO ---
        modelCarrello = new DefaultTableModel(
                new Object[]{"Tipo", "Nome", "Quantità", "Prezzo"}, 0);
        tabCarrello = new JTable(modelCarrello);

        // --- BOTTONI ---
        btnShowMarket = new JButton("Visualizza Marketplace");
        btnShowMarket.addActionListener(e -> ctrl.visualizzaMarketplace());

        btnShowCart = new JButton("Visualizza Carrello");
        btnShowCart.addActionListener(e -> aggiornaCarrello());

        initUI();
        aggiungiValidatoreQuantita();
        aggiungiListenerCheckboxEAzione();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(btnShowMarket);
        topPanel.add(btnShowCart);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabMarketplace),
                new JScrollPane(tabCarrello)
        );
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);
    }

    // --- Mostra Marketplace ---
    public void showMarketplace(List<Object> lista) {
        modelMarketplace.setRowCount(0);
        for (Object o : lista) {
            if (o instanceof Prodotto p) {
                modelMarketplace.addRow(new Object[]{
                        false, "Prodotto", p.getNome(), p.getDescrizione(),
                        p.getPrezzo(), p.getQuantita(), 0, ""
                });
            } else if (o instanceof Pacchetto pk) {
                modelMarketplace.addRow(new Object[]{
                        false, "Pacchetto", pk.getNome(), pk.getDescrizione(),
                        pk.getPrezzoTotale(), "-", 0, ""
                });
            }
        }
    }

    // --- Aggiorna Carrello ---
    private void aggiornaCarrello() {
        modelCarrello.setRowCount(0); // Svuota

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


    // --- Validatore Quantità ---
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
                            if (richiesta <= 0)
                                throw new IllegalArgumentException("⚠ La quantità deve essere almeno 1.");
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

    // --- Listener per Checkbox e Azione ---
    private void aggiungiListenerCheckboxEAzione() {
        // Listener per mostrare il bottone solo se spunto la checkbox
        tabMarketplace.getModel().addTableModelListener(e -> {
            int col = e.getColumn();
            int row = e.getFirstRow();
            if (col == 0 && row >= 0) { // Se cambio la checkbox
                Boolean selezionato = (Boolean) modelMarketplace.getValueAt(row, 0);
                if (Boolean.TRUE.equals(selezionato)) {
                    modelMarketplace.setValueAt("Aggiungi al carrello", row, 7);
                } else {
                    modelMarketplace.setValueAt("", row, 7);
                }
            }
        });

        // Listener per click sulla colonna Azione
        tabMarketplace.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabMarketplace.rowAtPoint(evt.getPoint());
                int col = tabMarketplace.columnAtPoint(evt.getPoint());

                if (col == 7) { // Colonna Azione
                    String azione = (String) modelMarketplace.getValueAt(row, 7);
                    if ("Aggiungi al carrello".equals(azione)) {
                        String tipo = (String) modelMarketplace.getValueAt(row, 1);
                        String nome = (String) modelMarketplace.getValueAt(row, 2);
                        int quantita = (Integer) modelMarketplace.getValueAt(row, 6);
                        Object disponibile = modelMarketplace.getValueAt(row, 5);

                        try {
                            ValidatoreMarketplace.validaTipo(tipo);
                            ValidatoreMarketplace.validaNome(nome);
                            if (tipo.equals("Prodotto")) {
                                int disponibileInt = disponibile instanceof Integer ? (Integer) disponibile : 0;
                                ValidatoreAcquisto.validaQuantita(quantita, disponibileInt);
                            } else {
                                if (quantita <= 0)
                                    throw new IllegalArgumentException("⚠ La quantità deve essere almeno 1.");
                            }
                            // Chiamo il controller per aggiungere al carrello
                            ctrl.aggiungiAlCarrello(nome, tipo, quantita);

                            avvisaSuccesso(nome + " aggiunto al carrello.");
                            aggiornaCarrello(); // aggiorna visuale carrello

                        } catch (IllegalArgumentException ex) {
                            avvisaErrore(ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    // --- Messaggi ---


    public void avvisaSuccesso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void avvisaErrore(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }


    public void showCarrello(List<Object[]> carrello) {
        modelCarrello.setRowCount(0); // Pulisci il carrello

        for (Object[] riga : carrello) {
            modelCarrello.addRow(riga);
        }
    }

}
