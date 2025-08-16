package unicam.filiera.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FormAnnuncioItemDialog extends JDialog {

    private static final int MAX_TITOLO = 100;
    private static final int MAX_TESTO = 1000;

    private final String nomeItem;
    private final JTextField txtTitolo = new JTextField(30);
    private final JTextArea txtTesto = new JTextArea(6, 30);
    private boolean confermato = false;

    public FormAnnuncioItemDialog(Window owner, String nomeItem) {
        super(owner, "Annuncio per: " + nomeItem, ModalityType.APPLICATION_MODAL);
        this.nomeItem = nomeItem;
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        // form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        form.add(new JLabel("Titolo* (max " + MAX_TITOLO + "):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtTitolo.setToolTipText("Obbligatorio, massimo " + MAX_TITOLO + " caratteri");
        form.add(txtTitolo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Testo* (max " + MAX_TESTO + "):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtTesto.setLineWrap(true);
        txtTesto.setWrapStyleWord(true);
        txtTesto.setToolTipText("Obbligatorio, massimo " + MAX_TESTO + " caratteri");
        form.add(new JScrollPane(txtTesto), gbc);

        // bottoni
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Annulla");
        ok.addActionListener(this::onOk);
        cancel.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok);
        south.add(cancel);

        // invio/esc
        getRootPane().setDefaultButton(ok);
        root.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        root.add(form, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void onOk(ActionEvent e) {
        String titolo = txtTitolo.getText().trim();
        String testo = txtTesto.getText().trim();

        if (titolo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il titolo è obbligatorio.", "Campi mancanti",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (testo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il testo è obbligatorio.", "Campi mancanti",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (titolo.length() > MAX_TITOLO) {
            JOptionPane.showMessageDialog(this, "Il titolo non può superare " + MAX_TITOLO + " caratteri.",
                    "Titolo troppo lungo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (testo.length() > MAX_TESTO) {
            JOptionPane.showMessageDialog(this, "Il testo non può superare " + MAX_TESTO + " caratteri.",
                    "Testo troppo lungo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confermato = true;
        dispose();
    }

    public boolean isConfermato() {
        return confermato;
    }

    public String getTitolo() {
        return txtTitolo.getText().trim();
    }

    public String getTesto() {
        return txtTesto.getText().trim();
    }

    public String getNomeItem() {
        return nomeItem;
    }
}
