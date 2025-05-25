package unicam.filiera.view;

import unicam.filiera.controller.CuratoreController;
import unicam.filiera.controller.ObserverManagerProdotto;
import unicam.filiera.controller.ObserverManagerProdottoTrasformato;
import unicam.filiera.model.*;
import unicam.filiera.model.observer.OsservatorePacchetto;
import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.controller.ObserverManagerPacchetto;
import unicam.filiera.model.observer.OsservatoreProdottoTrasformato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pannello per il ruolo Curatore: mostra prodotti e pacchetti da approvare.
 * Contiene solo la parte grafica (preview di foto e certificati).
 */
public class PannelloCuratore extends JPanel
        implements OsservatoreProdotto, OsservatorePacchetto, OsservatoreProdottoTrasformato
 {

    private final JTable tabella;
    private final DefaultTableModel model;
    private final JScrollPane scrollPane;
    private final JButton toggleButton;
    private final CuratoreController controller = new CuratoreController();

    public PannelloCuratore(UtenteAutenticato utente) {
        super(new BorderLayout());

        // Header
        JLabel benvenuto = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        // Colonne della tabella
        String[] col = {"Nome", "Descrizione", "Quantità", "Prezzo", "Indirizzo",
                "Creato da", "Certificati", "Foto",
                "Accetta", "Rifiuta", "Commento"};
        model = new DefaultTableModel(col, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Solo le colonne di preview (6,7) e approvazione (8,9,10)
                return col >= 6;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 6 || c == 7) return List.class;       // lista di File
                if (c == 8 || c == 9) return JButton.class;     // accetta/rifiuta
                return super.getColumnClass(c);
            }
        };

        tabella = new JTable(model);
        tabella.setRowHeight(40);

        // Preview certificati e foto
        PreviewCell preview = new PreviewCell();
        tabella.getColumn("Certificati").setCellRenderer(preview);
        tabella.getColumn("Certificati").setCellEditor(preview);
        tabella.getColumn("Foto").setCellRenderer(preview);
        tabella.getColumn("Foto").setCellEditor(preview);

        // Colonne di approvazione e commento
        tabella.getColumn("Accetta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Accetta").setCellEditor(new ComponentCellEditor());
        tabella.getColumn("Rifiuta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Rifiuta").setCellEditor(new ComponentCellEditor());
        // Commento usa default renderer (stringhe)

        // Scroll pane
        scrollPane = new JScrollPane(tabella);
        scrollPane.setVisible(false);
        add(scrollPane, BorderLayout.CENTER);

        // Bottone toggle
        toggleButton = new JButton("Visualizza elementi da approvare");
        toggleButton.addActionListener(e -> {
            if (!scrollPane.isVisible()) caricaElementiInAttesa();
            scrollPane.setVisible(!scrollPane.isVisible());
            toggleButton.setText(scrollPane.isVisible()
                    ? "Nascondi lista elementi"
                    : "Visualizza elementi da approvare");
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JFrame) ((JFrame) w).pack();
            revalidate();
            repaint();
        });
        add(toggleButton, BorderLayout.SOUTH);

        // Registrazione come osservatore
        ObserverManagerProdotto.registraOsservatore(this);
        ObserverManagerPacchetto.registraOsservatore(this);
        ObserverManagerProdottoTrasformato.registraOsservatore(this);


        // Carica iniziale
        caricaElementiInAttesa();
    }

    @Override
    public void notifica(Prodotto prodotto, String evento) {
        if ("NUOVO_PRODOTTO".equals(evento)) {
            SwingUtilities.invokeLater(this::caricaElementiInAttesa);
        }
    }

    @Override
    public void notifica(Pacchetto pacchetto, String evento) {
        if ("NUOVO_PACCHETTO".equals(evento)) {
            SwingUtilities.invokeLater(this::caricaElementiInAttesa);
        }
    }

    @Override
    public void notifica(ProdottoTrasformato prodotto, String evento) {
        if ("NUOVO_PRODOTTO_TRASFORMATO".equals(evento) ||
                "APPROVATO".equals(evento) ||
                "RIFIUTATO".equals(evento)) {
            SwingUtilities.invokeLater(this::caricaElementiInAttesa);
        }
    }


    private void caricaElementiInAttesa() {
        model.setRowCount(0);
        controller.getProdottiDaApprovare().forEach(this::aggiungiRiga);
        controller.getPacchettiDaApprovare().forEach(this::aggiungiRiga);
        controller.getProdottiTrasformatiDaApprovare().forEach(this::aggiungiRiga);
    }


    private void aggiungiRiga(Item item) {
        String nome;
        int qt;
        double pr;
        String certDir, fotoDir;

        if (item instanceof Pacchetto k) {
            nome = "[PAC] " + k.getNome();
            qt = k.getProdotti().size();
            pr = k.getPrezzoTotale();
            certDir = "uploads/certificati_pacchetti/";
            fotoDir = "uploads/foto_pacchetti/";
        } else if (item instanceof unicam.filiera.model.ProdottoTrasformato pt) {
            nome = "[TRASF] " + pt.getNome();
            qt = pt.getQuantita();
            pr = pt.getPrezzo();
            certDir = "uploads/certificati/"; // (o se vuoi, cartelle specifiche)
            fotoDir = "uploads/foto/";
        } else if (item instanceof Prodotto p) {
            nome = p.getNome();
            qt = p.getQuantita();
            pr = p.getPrezzo();
            certDir = "uploads/certificati/";
            fotoDir = "uploads/foto/";
        } else {
            nome = item.getNome();
            qt = 0;
            pr = 0;
            certDir = "";
            fotoDir = "";
        }

        String desc = item.getDescrizione();
        String ind = item.getIndirizzo();
        String cd = item.getCreatoDa();

        List<File> certFiles = item.getCertificati().stream()
                .map(name -> new File(certDir + name))
                .collect(Collectors.toList());
        List<File> fotoFiles = item.getFoto().stream()
                .map(name -> new File(fotoDir + name))
                .collect(Collectors.toList());

        JButton btnA = new JButton("✔");
        JButton btnR = new JButton("✖");

        btnA.addActionListener(e -> {
            int row = tabella.getEditingRow();
            if (tabella.isEditing()) tabella.getCellEditor().stopCellEditing();
            controller.valutaItem(item, true, null, (ok, msg) -> {
                if (ok && row >= 0 && row < model.getRowCount()) model.removeRow(row);
                JOptionPane.showMessageDialog(this, msg);
            });
        });

        btnR.addActionListener(e -> {
            int row = tabella.getEditingRow();
            if (tabella.isEditing()) tabella.getCellEditor().stopCellEditing();
            String commento = (String) model.getValueAt(row, 10);

            controller.valutaItem(item, false, commento, (ok, msg) -> {
                if (ok && row >= 0 && row < model.getRowCount()) model.removeRow(row);
                JOptionPane.showMessageDialog(this, msg);
            });
        });

        model.addRow(new Object[]{nome, desc, qt, pr, ind, cd, certFiles, fotoFiles, btnA, btnR, ""});
    }



    // Renderer/Editor per preview di file
    private class PreviewCell extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor, ActionListener {
        private final JButton button = new JButton("🔍");
        private List<File> files;
        private String column;

        public PreviewCell() {
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return button;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int col) {
            files = (List<File>) value;
            column = table.getColumnName(col);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return files;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stopCellEditing();
            showPreviewDialog(files, column);
        }
    }

    private void showPreviewDialog(List<File> files, String type) {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                type,
                Dialog.ModalityType.APPLICATION_MODAL
        );
        JPanel pnl = new JPanel(new GridLayout(0, 1, 5, 5));
        for (File f : files) {
            if (!f.exists()) {
                pnl.add(new JLabel("⚠ File non trovato: " + f.getName()));
                continue;
            }
            if ("Foto".equals(type)) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                JLabel lbl = new JLabel(icon);
                lbl.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                pnl.add(lbl);
            } else {
                JButton bf = new JButton(f.getName());
                bf.addActionListener(ae -> {
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                pnl.add(bf);
            }
        }
        dlg.add(new JScrollPane(pnl));
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // Dummy renderer/editor per Accetta/Rifiuta
    private static class ComponentCellEditor extends AbstractCellEditor implements TableCellEditor {
        @Override
        public Component getTableCellEditorComponent(
                JTable t, Object v, boolean s, int r, int c) {
            return (Component) v;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    private static class ComponentCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            if (v instanceof Component) {
                return (Component) v;
            } else {
                return new JLabel(v != null ? v.toString() : "");
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManagerProdotto.rimuoviOsservatore(this);
        ObserverManagerPacchetto.rimuoviOsservatore(this);
        ObserverManagerProdottoTrasformato.rimuoviOsservatore(this);

    }
}
