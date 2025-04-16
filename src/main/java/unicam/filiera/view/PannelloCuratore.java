package unicam.filiera.view;

import unicam.filiera.dao.ProdottoDAO;
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

    public PannelloCuratore(UtenteAutenticato utente) {
        setLayout(new BorderLayout());

        JLabel benvenuto = new JLabel("Benvenuto " + utente.getNome() + ", " + utente.getRuolo(), SwingConstants.CENTER);
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        // Colonne
        String[] colonne = {"Nome", "Descrizione", "Quantità", "Prezzo", "Creato da", "Accetta", "Rifiuta", "Commento"};
        model = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6 || column == 7;
            }
        };


        tabella = new JTable(model);
        tabella.setRowHeight(40);

        // Impostiamo sia il renderer che l'editor personalizzato sulle colonne "Accetta" e "Rifiuta"
        tabella.getColumn("Accetta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Accetta").setCellEditor(new ComponentCellEditor());

        tabella.getColumn("Rifiuta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Rifiuta").setCellEditor(new ComponentCellEditor());

        // Per la colonna "Commento" basta il renderer (se non vogliamo editarla con un componente particolare)
        tabella.getColumn("Commento").setCellRenderer(new ComponentCellRenderer());

        scrollPane = new JScrollPane(tabella);
        scrollPane.setVisible(false); // inizialmente nascosto
        add(scrollPane, BorderLayout.CENTER);

        // Bottone toggle visualizzazione
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
     * Carica tutti i prodotti in stato "IN_ATTESA" e li aggiunge alla tabella.
     */
    private void caricaProdottiInAttesa() {
        model.setRowCount(0); // Pulisce la tabella

        ProdottoDAO dao = new ProdottoDAO();
        List<Prodotto> prodottiInAttesa = dao.getProdottiByStato(StatoProdotto.IN_ATTESA);

        for (Prodotto p : prodottiInAttesa) {
            JButton btnAccetta = new JButton("✔");
            JButton btnRifiuta = new JButton("✖");

            // Aggiungiamo una nuova riga con i valori e i due JButton
            model.addRow(new Object[]{
                    p.getNome(),
                    p.getDescrizione(),
                    p.getQuantita(),
                    p.getPrezzo(),
                    p.getCreatoDa(),
                    btnAccetta,
                    btnRifiuta,
                    ""
            });

            int rowIndex = model.getRowCount() - 1;

            // Listener per pulsante “Accetta”
            btnAccetta.addActionListener(e -> {
                boolean success = dao.aggiornaStatoProdotto(p, StatoProdotto.APPROVATO);
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
                String commento = (String) model.getValueAt(rowIndex, 7);
                dao.aggiornaStatoProdotto(p, StatoProdotto.RIFIUTATO);
                JOptionPane.showMessageDialog(this,
                        "Prodotto rifiutato!" + (commento.isEmpty() ? "" : "\nCommento: " + commento));
                caricaProdottiInAttesa();
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
            return (Component) value;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
