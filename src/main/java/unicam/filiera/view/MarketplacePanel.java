package unicam.filiera.view;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.model.Prodotto;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MarketplacePanel extends JPanel {

    private final DefaultListModel<String> modelloLista = new DefaultListModel<>();
    private final JList<String> listaProdotti = new JList<>(modelloLista);
    private final JTextArea dettagliArea = new JTextArea();
    private final JButton btnIndietro = new JButton("Torna alla Home");

    private final MarketplaceController controller;

    public MarketplacePanel(MarketplaceController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());

        // Lista dei nomi prodotti
        listaProdotti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(listaProdotti), BorderLayout.WEST);

        // Area dettagli
        dettagliArea.setEditable(false);
        add(new JScrollPane(dettagliArea), BorderLayout.CENTER);

        // Bottone per chiudere
        JPanel sud = new JPanel();
        sud.add(btnIndietro);
        add(sud, BorderLayout.SOUTH);

        listaProdotti.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selezionato = listaProdotti.getSelectedValue();
                if (selezionato != null) {
                    Prodotto prodotto = controller.espandiProdotto(selezionato);
                    mostraDettagliProdotto(prodotto);
                }
            }
        });
    }

    // Usato dal controller per mostrare i prodotti
    public void mostraProdotti(List<Prodotto> prodotti) {
        modelloLista.clear();
        for (Prodotto p : prodotti) {
            modelloLista.addElement(p.getNome());
        }
    }

    // Espandi prodotto selezionato
    private void mostraDettagliProdotto(Prodotto p) {
        String dettagli = """
                Nome: %s
                Descrizione: %s
                Prezzo: %.2f €
                Quantità: %d
                Certificati: %s
                Foto: %s
                """.formatted(
                p.getNome(),
                p.getDescrizione(),
                p.getPrezzo(),
                p.getQuantita(),
                String.join(", ", p.getCertificati()),
                String.join(", ", p.getFoto())
        );
        dettagliArea.setText(dettagli);
    }

    public JButton getBtnIndietro() {
        return btnIndietro;
    }
}
