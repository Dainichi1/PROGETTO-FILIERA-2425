package unicam.filiera.view;

import unicam.filiera.dto.PostSocialDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog per l'inserimento di una recensione social.
 */
public class FormRecensioneDialog extends JDialog {

    private final JTextField txtTitolo = new JTextField(30);
    private final JTextArea txtTesto = new JTextArea(5, 30);
    private boolean confermato = false;

    private final PostSocialDto post = new PostSocialDto();

    public FormRecensioneDialog(Window owner, int idAcquisto, String nomeItem, String tipoItem, String autoreUsername) {
        super(owner, "Pubblica recensione su social", ModalityType.APPLICATION_MODAL);

        // Precompila dati non modificabili
        post.setIdAcquisto(idAcquisto);
        post.setNomeItem(nomeItem);
        post.setTipoItem(tipoItem);
        post.setAutoreUsername(autoreUsername);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));

        // --- Campi form ---
        JPanel pnlFields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        pnlFields.add(new JLabel("Titolo:"), gbc);
        gbc.gridx = 1;
        pnlFields.add(txtTitolo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        pnlFields.add(new JLabel("Testo:"), gbc);
        gbc.gridx = 1;
        JScrollPane scrollText = new JScrollPane(txtTesto);
        pnlFields.add(scrollText, gbc);

        pnlMain.add(pnlFields, BorderLayout.CENTER);

        // --- Bottoni ---
        JPanel pnlButtons = new JPanel();
        JButton btnOk = new JButton("Pubblica");
        JButton btnCancel = new JButton("Annulla");

        btnOk.addActionListener(this::onPubblica);
        btnCancel.addActionListener(e -> dispose());

        pnlButtons.add(btnOk);
        pnlButtons.add(btnCancel);
        pnlMain.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(pnlMain);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Gestione click sul pulsante Pubblica.
     */
    private void onPubblica(ActionEvent e) {
        salvaDatiNelDto();
        String errori = validaNelDialog();

        if (errori != null) {
            JOptionPane.showMessageDialog(
                    this,
                    errori,
                    "Dati mancanti o non validi",
                    JOptionPane.WARNING_MESSAGE
            );
            return; // Non chiudere il dialog: loop finché non è valido
        }

        confermato = true;
        dispose();
    }

    /**
     * Copia i dati inseriti nei campi nel DTO.
     */
    private void salvaDatiNelDto() {
        post.setTitolo(txtTitolo.getText().trim());
        post.setTesto(txtTesto.getText().trim());
    }

    /**
     * Validazione base dei campi direttamente nel dialog.
     *
     * @return Stringa con gli errori, oppure null se tutto valido.
     */
    private String validaNelDialog() {
        StringBuilder sb = new StringBuilder();

        if (post.getTitolo() == null || post.getTitolo().isBlank()) {
            sb.append("• Il titolo è obbligatorio.\n");
        } else if (post.getTitolo().length() > 100) {
            sb.append("• Il titolo non può superare 100 caratteri.\n");
        }

        if (post.getTesto() == null || post.getTesto().isBlank()) {
            sb.append("• Il testo della recensione è obbligatorio.\n");
        } else if (post.getTesto().length() > 1000) {
            sb.append("• Il testo non può superare 1000 caratteri.\n");
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    public boolean isConfermato() {
        return confermato;
    }

    public PostSocialDto getPostSocialDto() {
        return post;
    }
}
