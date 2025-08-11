package unicam.filiera.view;

import unicam.filiera.dto.AnnuncioEventoDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog per inserire i dettagli dell'annuncio per Fiera/Visita.
 * Valida i dati e, se confermati, espone il DTO tramite getter.
 */
public class FormAnnuncioEventoDialog extends JDialog {

    private final JTextField txtTitolo = new JTextField(30);
    private final JTextArea txtTesto = new JTextArea(6, 30);
    private boolean confermato = false;

    private final AnnuncioEventoDto annuncio = new AnnuncioEventoDto();

    public FormAnnuncioEventoDialog(Window owner, long eventoId, String tipoEvento /* "FIERA"|"VISITA" */
    ) {
        super(owner, "Dettagli annuncio", ModalityType.APPLICATION_MODAL);

        annuncio.setEventoId(eventoId);
        annuncio.setTipoEvento(tipoEvento);


        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Titolo*:"), gbc);
        gbc.gridx = 1;
        form.add(txtTitolo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Testo*:"), gbc);
        gbc.gridx = 1;
        txtTesto.setLineWrap(true);
        txtTesto.setWrapStyleWord(true);
        form.add(new JScrollPane(txtTesto), gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Annulla");
        btnOk.addActionListener(this::onOk);
        btnCancel.addActionListener(e -> dispose());
        buttons.add(btnOk);
        buttons.add(btnCancel);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void onOk(ActionEvent e) {
        salvaNelDto();
        String err = valida();
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Campi mancanti/non validi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        confermato = true;
        dispose();
    }

    private void salvaNelDto() {
        annuncio.setTitolo(txtTitolo.getText().trim());
        annuncio.setTesto(txtTesto.getText().trim());
    }

    /**
     * Ritorna stringa errori o null se valido
     */
    private String valida() {
        StringBuilder sb = new StringBuilder();
        if (annuncio.getTitolo() == null || annuncio.getTitolo().isBlank())
            sb.append("• Il titolo è obbligatorio.\n");
        else if (annuncio.getTitolo().length() > 100)
            sb.append("• Il titolo non può superare 100 caratteri.\n");

        if (annuncio.getTesto() == null || annuncio.getTesto().isBlank())
            sb.append("• Il testo è obbligatorio.\n");
        else if (annuncio.getTesto().length() > 1000)
            sb.append("• Il testo non può superare 1000 caratteri.\n");

        return sb.length() == 0 ? null : sb.toString();
    }

    public boolean isConfermato() {
        return confermato;
    }

    public AnnuncioEventoDto getAnnuncio() {
        return annuncio;
    }
}
