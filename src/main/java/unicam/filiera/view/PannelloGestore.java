package unicam.filiera.view;

import unicam.filiera.controller.GestoreRichiesteEliminazioneController;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.UtenteAutenticato;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PannelloGestore extends JPanel {

    private final UtenteAutenticato utente;
    private final GestoreRichiesteEliminazioneController richiesteController =
            new GestoreRichiesteEliminazioneController();

    public PannelloGestore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;

        // Header "Benvenuto ..."
        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER
        );
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        benv.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(benv, BorderLayout.NORTH);

        // Barra superiore comandi
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRichieste = new JButton("Visualizza richieste di eliminazione in attesa");
        topBar.add(btnRichieste);
        add(topBar, BorderLayout.SOUTH);

        btnRichieste.addActionListener(e -> mostraRichiesteInAttesa());

        // placeholder centro
        JPanel center = new JPanel(new GridBagLayout());
        center.add(new JLabel("Dashboard Gestore"));
        add(center, BorderLayout.CENTER);
    }

    /** Mostra un dialog semplice con le richieste IN_ATTESA. */
    private void mostraRichiesteInAttesa() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Richieste di eliminazione in attesa", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());

        // tabella
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Username", "Data richiesta", "Stato"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        // pannello bottoni
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApri = new JButton("Apri dettagli");
        JButton btnChiudi = new JButton("Chiudi");
        south.add(btnApri);
        south.add(btnChiudi);
        dlg.add(south, BorderLayout.SOUTH);

        // loader dei dati
        Runnable load = () -> {
            model.setRowCount(0);
            var richieste = richiesteController.getRichiesteInAttesa();
            if (richieste.isEmpty()) {
                // svuota e mostra info
                JOptionPane.showMessageDialog(dlg, "Nessuna richiesta in attesa.",
                        "Richieste eliminazione", JOptionPane.INFORMATION_MESSAGE);
            }
            var fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var r : richieste) {
                model.addRow(new Object[]{
                        r.getId(),
                        r.getUsername(),
                        r.getDataRichiesta() != null ? r.getDataRichiesta().format(fmt) : "",
                        r.getStato().name()
                });
            }
        };
        load.run();

        btnApri.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Seleziona una richiesta dalla lista.");
                return;
            }
            int richiestaId = (int) model.getValueAt(row, 0);
            var det = new DettaglioRichiestaEliminazioneDialog(
                    dlg,
                    richiesteController,
                    richiestaId,
                    load::run // callback: ricarica la lista dopo Approva/Rifiuta
            );
            det.setVisible(true);
        });

        btnChiudi.addActionListener(e -> dlg.dispose());

        dlg.setSize(800, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
