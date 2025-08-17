package unicam.filiera.view;

import unicam.filiera.repository.AddressInfo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SelezionaIndirizziDialog extends JDialog {
    private final List<JCheckBox> checkBoxList = new ArrayList<>();
    private final List<AddressInfo> addresses;
    private final JButton btnOk = new JButton("OK");
    private final JButton btnAnnulla = new JButton("Annulla");
    private List<AddressInfo> selectedAddresses = new ArrayList<>();

    public SelezionaIndirizziDialog(Window parent) {
        super(parent, "Seleziona indirizzi per la mappa", ModalityType.APPLICATION_MODAL);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Recupera tutti gli indirizzi disponibili
        this.addresses = unicam.filiera.repository.AddressRepository.findAllDistinctAddressesWithSource();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);

        for (AddressInfo info : addresses) {
            JCheckBox checkBox = new JCheckBox(info.toString());
            checkBoxList.add(checkBox);
            panel.add(checkBox);
        }

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(btnOk);
        buttonsPanel.add(btnAnnulla);

        btnOk.addActionListener(e -> {
            selectedAddresses = new ArrayList<>();
            for (int i = 0; i < checkBoxList.size(); i++) {
                if (checkBoxList.get(i).isSelected()) {
                    selectedAddresses.add(addresses.get(i));
                }
            }
            setVisible(false);
            dispose();
        });

        btnAnnulla.addActionListener(e -> {
            selectedAddresses = new ArrayList<>();
            setVisible(false);
            dispose();
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel("Seleziona gli indirizzi da visualizzare come marker:"), BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
    }

    public List<AddressInfo> getSelectedAddresses() {
        return selectedAddresses;
    }
}
