package unicam.filiera.view;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.model.Prodotto;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PannelloMarketplace extends JPanel {

    private final MarketplaceController controller = new MarketplaceController();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> listaProdotti = new JList<>(listModel);
    private final JTextArea dettagliArea = new JTextArea();
    private final JPanel contenitore;

    private List<Prodotto> prodotti; // cache

    public PannelloMarketplace(JFrame parentFrame) {
        setLayout(new BorderLayout());

        JLabel titolo = new JLabel("Marketplace - Prodotti approvati", SwingConstants.CENTER);
        titolo.setFont(new Font("Arial", Font.BOLD, 16));
        add(titolo, BorderLayout.NORTH);

        contenitore = new JPanel(new GridLayout(1, 2, 10, 10));

        JScrollPane listaScroll = new JScrollPane(listaProdotti);
        contenitore.add(listaScroll);

        dettagliArea.setEditable(false);
        dettagliArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane dettagliScroll = new JScrollPane(dettagliArea);
        contenitore.add(dettagliScroll);

        add(contenitore, BorderLayout.CENTER);

        // 🔙 Bottone per tornare alla home
        JButton btnTornaHome = new JButton("Torna alla Home");
        btnTornaHome.addActionListener(e -> {
            if (parentFrame instanceof MainWindow mainWindow) {
                mainWindow.tornaAllaHome();
            }
        });
        add(btnTornaHome, BorderLayout.SOUTH);

        // Listener selezione prodotto
        listaProdotti.addListSelectionListener(e -> {
            int index = listaProdotti.getSelectedIndex();
            if (index >= 0 && index < prodotti.size()) {
                Prodotto p = prodotti.get(index);
                dettagliArea.setText(
                        "Nome: " + p.getNome() + "\n" +
                                "Descrizione: " + p.getDescrizione() + "\n" +
                                "Prezzo: " + p.getPrezzo() + " €\n" +
                                "Quantità: " + p.getQuantita() + "\n" +
                                "Creato da: " + p.getCreatoDa()
                );
            }
        });

        aggiornaLista(); // carica inizialmente
    }

    public void aggiornaLista() {
        listModel.clear();
        dettagliArea.setText("");
        prodotti = controller.getProdottiApprovati();
        for (Prodotto p : prodotti) {
            listModel.addElement(p.getNome());
        }
    }
}
