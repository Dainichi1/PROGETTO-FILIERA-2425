package unicam.filiera.view;

import unicam.filiera.controller.CategoriaContenuto;
import unicam.filiera.controller.CriteriRicerca;
import unicam.filiera.controller.GestoreContenutiController;
import unicam.filiera.dto.ElementoPiattaformaDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PannelloGestoreContenuti extends JPanel {

    private final GestoreContenutiController controller = new GestoreContenutiController();

    // sinistra: categorie
    private final DefaultListModel<CategoriaContenuto> listModel = new DefaultListModel<>();
    private final JList<CategoriaContenuto> listaCategorie = new JList<>(listModel);

    // destra: header + filtri + tabella
    private final JLabel lblTitolo = new JLabel("Seleziona una categoria");
    private final JTextField txtRicerca = new JTextField(18);
    private final JComboBox<String> comboStato = new JComboBox<>();
    private final JComboBox<String> comboOrdina = new JComboBox<>(new String[]{"DATA", "NOME", "STATO"});
    private final JCheckBox chkAsc = new JCheckBox("Asc", true);
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Nome", "Tipo", "Stato", "Data", "Extra"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private List<ElementoPiattaformaDto> listaCorrente;
    private CategoriaContenuto categoriaSelezionata;

    public PannelloGestoreContenuti() {
        super(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10,10,10,10));

        // pannello sinistra
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Categorie", SwingConstants.CENTER), BorderLayout.NORTH);
        listaCategorie.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(new JScrollPane(listaCategorie), BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        // pannello destra
        JPanel right = new JPanel(new BorderLayout(8,8));
        lblTitolo.setFont(lblTitolo.getFont().deriveFont(Font.BOLD, 16f));
        right.add(lblTitolo, BorderLayout.NORTH);

        // filtri
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filters.add(new JLabel("Ricerca"));
        filters.add(txtRicerca);
        filters.add(new JLabel("Stato"));
        filters.add(comboStato);
        filters.add(new JLabel("Ordina per"));
        filters.add(comboOrdina);
        filters.add(chkAsc);
        JButton btnApplica = new JButton("Applica");
        filters.add(btnApplica);
        right.add(filters, BorderLayout.SOUTH);

        // tabella
        table.setAutoCreateRowSorter(true);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        add(right, BorderLayout.CENTER);

        // footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDettagli = new JButton("Dettagli");
        JButton btnRiepilogo = new JButton("Torna al riepilogo");
        footer.add(btnRiepilogo);
        footer.add(btnDettagli);
        add(footer, BorderLayout.SOUTH);

        // Populate categorie
        controller.getCategorieContenuti().forEach(listModel::addElement);

        // listeners
        listaCategorie.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                categoriaSelezionata = listaCategorie.getSelectedValue();
                if (categoriaSelezionata != null) {
                    caricaCategoria(categoriaSelezionata);
                }
            }
        });

        btnApplica.addActionListener(e -> applicaFiltri());
        btnRiepilogo.addActionListener(e -> mostraRiepilogo());
        btnDettagli.addActionListener(e -> mostraDettagli());

        // stato iniziale
        mostraRiepilogo();
    }

    private void mostraRiepilogo() {
        lblTitolo.setText("Riepilogo categorie: seleziona una categoria");
        comboStato.setModel(new DefaultComboBoxModel<>(new String[]{"Tutti"}));
        tableModel.setRowCount(0);
        listaCategorie.clearSelection();
        listaCorrente = null;
    }

    private void caricaCategoria(CategoriaContenuto cat) {
        lblTitolo.setText("Categoria: " + cat.label());
        comboStato.setModel(new DefaultComboBoxModel<>(controller.getPossibiliStati(cat)));

        listaCorrente = controller.getContenutiCategoria(cat);
        tableModel.setRowCount(0);

        if (listaCorrente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nessun elemento presente in questa categoria.",
                    "Informazione", JOptionPane.INFORMATION_MESSAGE);
            // Ramo "torna al riepilogo" esplicito
            int opt = JOptionPane.showOptionDialog(this,
                    "Vuoi tornare al riepilogo o selezionare un’altra categoria?",
                    "Scelta",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Torna al riepilogo", "Seleziona altra categoria"},
                    "Torna al riepilogo");
            if (opt == 0) {
                mostraRiepilogo();
                return;
            } else {
                // lascia la lista categorie selezionabile
                return;
            }
        }

        var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (ElementoPiattaformaDto e : listaCorrente) {
            tableModel.addRow(new Object[]{
                    e.getId(),
                    e.getNome(),
                    e.getTipo(),
                    e.getStato(),
                    e.getData() != null ? fmt.format(e.getData().toLocalDateTime()) : "",
                    e.getExtra()
            });
        }
    }

    private void applicaFiltri() {
        if (listaCorrente == null) return;
        CriteriRicerca cr = new CriteriRicerca();
        cr.testo = txtRicerca.getText();
        cr.stato = (String) comboStato.getSelectedItem();
        cr.orderBy = (String) comboOrdina.getSelectedItem();
        cr.asc = chkAsc.isSelected();

        List<ElementoPiattaformaDto> filtrata = controller.filtraOrdinaLista(listaCorrente, cr);
        tableModel.setRowCount(0);
        var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (ElementoPiattaformaDto e : filtrata) {
            tableModel.addRow(new Object[]{
                    e.getId(),
                    e.getNome(),
                    e.getTipo(),
                    e.getStato(),
                    e.getData() != null ? fmt.format(e.getData().toLocalDateTime()) : "",
                    e.getExtra()
            });
        }
    }

    private void mostraDettagli() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un elemento.");
            return;
        }
        // la JTable può essere ordinata: mappa riga view -> model
        int modelRow = table.convertRowIndexToModel(r);

        String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String nome = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String tipo = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String stato= String.valueOf(tableModel.getValueAt(modelRow, 3));
        String data = String.valueOf(tableModel.getValueAt(modelRow, 4));
        String extra= String.valueOf(tableModel.getValueAt(modelRow, 5));

        JTextArea area = new JTextArea(
                "ID: " + id + "\n" +
                        "Nome/Titolo: " + nome + "\n" +
                        "Tipo: " + tipo + "\n" +
                        "Stato: " + stato + "\n" +
                        "Data: " + data + "\n" +
                        "Info: " + extra + "\n"
        );
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(520, 220));
        JOptionPane.showMessageDialog(this, sp, "Dettagli elemento", JOptionPane.INFORMATION_MESSAGE);
    }
}
