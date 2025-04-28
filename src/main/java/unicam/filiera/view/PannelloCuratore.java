package unicam.filiera.view;

import unicam.filiera.controller.CuratoreController;
import unicam.filiera.controller.ObserverManager;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.UtenteAutenticato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.model.Prodotto;

public class PannelloCuratore extends JPanel implements OsservatoreProdotto {

    private final JTable tabella;
    private final DefaultTableModel model;
    private final JScrollPane scrollPane;
    private final JButton toggleButton;

    // Riferimento al controller
    private final CuratoreController curatoreController;

    public PannelloCuratore(UtenteAutenticato utente) {
        setLayout(new BorderLayout());

        this.curatoreController = new CuratoreController();

        JLabel benvenuto = new JLabel("Benvenuto " + utente.getNome() + ", " + utente.getRuolo(), SwingConstants.CENTER);
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        // Colonne
        String[] colonne = {"Nome", "Descrizione", "Quantità", "Prezzo", "Indirizzo", "Creato da", "Certificati", "Foto", "Accetta", "Rifiuta", "Commento"};
        model = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7 || column == 8 || column == 9;
            }
        };

        tabella = new JTable(model);
        tabella.setRowHeight(40);

        // Renderer + Editor per pulsanti
        tabella.getColumn("Accetta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Accetta").setCellEditor(new ComponentCellEditor());

        tabella.getColumn("Rifiuta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Rifiuta").setCellEditor(new ComponentCellEditor());

        tabella.getColumn("Commento").setCellRenderer(new ComponentCellRenderer());

        scrollPane = new JScrollPane(tabella);
        scrollPane.setVisible(false);
        add(scrollPane, BorderLayout.CENTER);

        // Bottone toggle
        toggleButton = new JButton("Visualizza prodotti in attesa di approvazione");
        toggleButton.addActionListener(e -> {
            boolean visibile = scrollPane.isVisible();

            if (!visibile) {
                caricaProdottiInAttesa();
            }

            scrollPane.setVisible(!visibile);
            toggleButton.setText(!visibile
                    ? "Nascondi lista prodotti"
                    : "Visualizza prodotti in attesa di approvazione");

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame frame) {
                frame.pack();
            }

            this.revalidate();
            this.repaint();
        });

        add(toggleButton, BorderLayout.SOUTH);

        //  REGISTRA QUESTO PANNELLO COME OSSERVATORE
        ObserverManager.registraOsservatore(this);
        caricaProdottiInAttesa();
    }

    @Override
    public void notifica(Prodotto prodotto, String evento) {
        if ("NUOVO_PRODOTTO".equals(evento)) {
            SwingUtilities.invokeLater(this::caricaProdottiInAttesa);
        }
    }

    /**
     * Carica tutti i prodotti in stato "IN_ATTESA" dal controller e li aggiunge alla tabella.
     */
    private void caricaProdottiInAttesa() {
        model.setRowCount(0); // Pulisce la tabella

        List<Prodotto> prodottiInAttesa = curatoreController.getProdottiDaApprovare();

        for (Prodotto p : prodottiInAttesa) {
            // Crea i JButton
            JButton btnAccetta = new JButton("✔");
            JButton btnRifiuta = new JButton("✖");

            model.addRow(new Object[]{
                    p.getNome(),
                    p.getDescrizione(),
                    p.getQuantita(),
                    p.getPrezzo(),
                    p.getIndirizzo(),
                    p.getCreatoDa(),
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    btnAccetta,
                    btnRifiuta,
                    "" // campo commento inizialmente vuoto
            });

            int rowIndex = model.getRowCount() - 1;

            // Listener per pulsante “Accetta”
            btnAccetta.addActionListener(e -> {
                if (tabella.isEditing()) tabella.getCellEditor().stopCellEditing();

                boolean success = curatoreController.approvaProdotto(p);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Prodotto approvato!");
                    model.removeRow(rowIndex); // Rimuove la riga dalla tabella
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Errore durante l'approvazione!",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            });


            // Listener per pulsante “Rifiuta”
            btnRifiuta.addActionListener(e -> {
                if (tabella.isEditing()) tabella.getCellEditor().stopCellEditing();

                String commento = (String) model.getValueAt(rowIndex, 9);
                boolean success = curatoreController.rifiutaProdotto(p, commento);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Prodotto rifiutato!" + ((commento != null && !commento.isEmpty())
                                    ? "\nCommento: " + commento : ""));
                    caricaProdottiInAttesa(); // Ricarica lista
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Errore durante il rifiuto!",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

        }
    }

    /**
     * Classe interna che gestisce l'editor di cella per mostrare e rendere cliccabili i pulsanti.
     */
    private static class ComponentCellEditor extends AbstractCellEditor implements TableCellEditor {
        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            // value è il JButton
            return (Component) value;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this);
    }

}
