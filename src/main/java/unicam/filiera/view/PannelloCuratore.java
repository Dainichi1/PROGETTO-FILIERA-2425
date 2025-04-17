package unicam.filiera.view;

import unicam.filiera.controller.CuratoreController;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.UtenteAutenticato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class PannelloCuratore extends JPanel {

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
        String[] colonne = {"Nome", "Descrizione", "Quantità", "Prezzo", "Creato da", "Certificati", "Foto", "Accetta", "Rifiuta", "Commento"};
        model = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6 || column == 7;
            }
        };

        tabella = new JTable(model);
        tabella.setRowHeight(40);

        // Renderer + Editor personalizzati per i pulsanti Accetta / Rifiuta
        tabella.getColumn("Accetta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Accetta").setCellEditor(new ComponentCellEditor());

        tabella.getColumn("Rifiuta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Rifiuta").setCellEditor(new ComponentCellEditor());

        tabella.getColumn("Commento").setCellRenderer(new ComponentCellRenderer());

        scrollPane = new JScrollPane(tabella);
        scrollPane.setVisible(false); // inizialmente nascosto
        add(scrollPane, BorderLayout.CENTER);

        // Bottone toggle visualizzazione
        toggleButton = new JButton("Visualizza prodotti in attesa di approvazione");
        toggleButton.addActionListener(e -> {
            boolean visibile = scrollPane.isVisible();

            // Se era nascosta, carica i prodotti
            if (!visibile) {
                caricaProdottiInAttesa();
            }

            // Mostra/nasconde lo scrollPane
            scrollPane.setVisible(!visibile);
            toggleButton.setText(!visibile
                    ? "Nascondi lista prodotti"
                    : "Visualizza prodotti in attesa di approvazione");

            // Forza l’aggiornamento del frame padre
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame frame) {
                frame.pack(); // ridimensiona il frame per aggiornare il layout
            }

            this.revalidate();
            this.repaint();
        });

        add(toggleButton, BorderLayout.SOUTH);
    }

    /**
     * Carica tutti i prodotti in stato "IN_ATTESA" dal controller e li aggiunge alla tabella.
     */
    private void caricaProdottiInAttesa() {
        model.setRowCount(0); // Pulisce la tabella

        List<Prodotto> prodottiInAttesa = curatoreController.getProdottiInAttesa();

        for (Prodotto p : prodottiInAttesa) {
            // Crea i JButton
            JButton btnAccetta = new JButton("✔");
            JButton btnRifiuta = new JButton("✖");

            model.addRow(new Object[]{
                    p.getNome(),
                    p.getDescrizione(),
                    p.getQuantita(),
                    p.getPrezzo(),
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

                String commento = (String) model.getValueAt(rowIndex, 7);
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
}
