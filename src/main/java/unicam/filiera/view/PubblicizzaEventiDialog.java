package unicam.filiera.view;

import unicam.filiera.controller.AnimatoreController;
import unicam.filiera.dto.AnnuncioEventoDto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class PubblicizzaEventiDialog extends JDialog {

    private final AnimatoreController controller;
    private Consumer<AnnuncioEventoDto> onAnnuncioPronto;
    private final JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Fiere", "Visite su invito"});
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Titolo/Descrizione", "Data inizio", "Data fine", "Stato"}, 0
    );
    private final JButton btnPubblica = new JButton("Pubblica avviso su Social");
    private final JButton btnChiudi = new JButton("Chiudi");

    public PubblicizzaEventiDialog(Window owner, AnimatoreController controller) {
        super(owner, "Fiere/Visite pubblicate", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 480));
        setLocationRelativeTo(owner);

        buildUI();
        wireEvents();
    }

    private void buildUI() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Tipo:"));
        north.add(comboTipo);

        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPubblica.setEnabled(false); // si abilita solo con una selezione
        south.add(btnPubblica);
        south.add(btnChiudi);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
    }

    public void setOnAnnuncioPronto(Consumer<AnnuncioEventoDto> l) {
        this.onAnnuncioPronto = l;
    }

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnPubblica.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        // Per ora non fa nulla: la collegheremo al controller al passo successivo
        btnPubblica.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;

            long eventoId = ((Number) model.getValueAt(row, 0)).longValue();
            String tipoUi = getSelectedTipo(); // "Fiere" | "Visite su invito"
            String tipoEvento = "Fiere".equals(tipoUi) ? "FIERA" : "VISITA";



            FormAnnuncioEventoDialog form = new FormAnnuncioEventoDialog(
                    SwingUtilities.getWindowAncestor(this), eventoId, tipoEvento
            );
            form.setVisible(true);

            if (form.isConfermato() && onAnnuncioPronto != null) {
                onAnnuncioPronto.accept(form.getAnnuncio());
            }
        });

        btnChiudi.addActionListener(e -> dispose());
    }

    /* Metodi di popolamento che useremo al passo 2 */
    public void clearRows() { model.setRowCount(0); }

    public void addRow(Object id, String titolo, String inizio, String fine, String stato) {
        model.addRow(new Object[]{id, titolo, inizio, fine, stato});
    }

    public String getSelectedTipo() {
        return (String) comboTipo.getSelectedItem(); // "Fiere" | "Visite su invito"
    }

    public void addTipoChangeListener(ActionListener l) {
        comboTipo.addActionListener(ev -> {
            btnPubblica.setEnabled(false);
            l.actionPerformed(ev);
        });
    }



    public void setRows(java.util.List<Object[]> rows) {
        clearRows();
        for (Object[] r : rows) model.addRow(r);
    }
}
