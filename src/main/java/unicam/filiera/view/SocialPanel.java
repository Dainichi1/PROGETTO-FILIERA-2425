package unicam.filiera.view;

import unicam.filiera.dao.DatabaseManager;
import unicam.filiera.dao.JdbcSocialPostDAO;
import unicam.filiera.dto.PostSocialDto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

/**
 * Pannello che mostra il feed del Social Network.
 * Usa le classi esistenti: DatabaseManager, JdbcSocialPostDAO, PostSocialDto.
 */
public class SocialPanel extends JPanel {

    private final JButton btnIndietro = new JButton("Indietro");
    private final JButton btnAggiorna = new JButton("Aggiorna");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Data/Ora", "Autore", "Tipo", "Nome Item", "Titolo", "Testo"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabella = new JTable(model);

    public SocialPanel(Window owner) {
        super(new BorderLayout());

        // Barra superiore
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Feed globale dei post pubblicati sui social"));
        top.add(Box.createHorizontalStrut(16));
        top.add(btnAggiorna);
        top.add(Box.createHorizontalStrut(8));
        top.add(btnIndietro);
        add(top, BorderLayout.NORTH);

        // Tabella
        tabella.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        // Azioni
        btnAggiorna.addActionListener(e -> caricaPost(owner));

        // Caricamento iniziale
        caricaPost(owner);
    }

    /** Espone il pulsante Indietro per essere agganciato dalla MainWindow. */
    public JButton getBtnIndietro() {
        return btnIndietro;
    }

    /** Carica e popola la tabella con tutti i post, più recenti per primi. */
    private void caricaPost(Window owner) {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseManager.getConnection()) {
                JdbcSocialPostDAO dao = new JdbcSocialPostDAO(conn);
                List<PostSocialDto> posts = dao.findAllOrderByDataDesc();

                if (posts != null && !posts.isEmpty()) {
                    for (PostSocialDto p : posts) {
                        model.addRow(new Object[]{
                                p.getCreatedAt(),
                                p.getAutoreUsername(),
                                p.getTipoItem(),
                                p.getNomeItem(),
                                p.getTitolo(),
                                p.getTesto()
                        });
                    }
                } else {
                    // Nessun contenuto: mostro una riga "vuota" indicativa
                    model.addRow(new Object[]{"", "", "", "", "", "Nessun contenuto da mostrare."});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        owner,
                        "Errore nel caricamento del social network",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
    }
}
