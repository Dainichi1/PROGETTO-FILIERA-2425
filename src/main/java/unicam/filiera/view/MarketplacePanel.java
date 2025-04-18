package unicam.filiera.view;

import unicam.filiera.controller.MarketplaceController;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.observer.OsservatoreProdotto;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.controller.ObserverManager;


public class MarketplacePanel extends JPanel implements OsservatoreProdotto {


    private final DefaultListModel<String> modelloLista = new DefaultListModel<>();
    private final JList<String> listaProdotti = new JList<>(modelloLista);
    private final JTextArea dettagliArea = new JTextArea();
    private final JButton btnIndietro = new JButton("Torna alla Home");

    private final MarketplaceController controller;
    private final JFrame frameChiamante; // Aggiunto campo

    public MarketplacePanel(JFrame frameChiamante, MarketplaceController controller) {
        this.frameChiamante = frameChiamante; // ✅ Inizializzazione
        this.controller = controller;

        setLayout(new BorderLayout());

        // Lista prodotti (a sinistra)
        listaProdotti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(listaProdotti), BorderLayout.WEST);

        // Area Dettagli (a destra)
        dettagliArea.setEditable(false);
        add(new JScrollPane(dettagliArea), BorderLayout.CENTER);

        // Pannello sud con bottone
        JPanel sud = new JPanel();
        sud.add(btnIndietro);
        add(sud, BorderLayout.SOUTH);

        // ✅ Bottone indietro
        btnIndietro.addActionListener(e -> {
            if (frameChiamante instanceof MainWindow main) {
                main.tornaAllaHome();
            }
        });

        // Azione selezione prodotto
        listaProdotti.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selezionato = listaProdotti.getSelectedValue();
                if (selezionato != null) {
                    Prodotto prodotto = controller.espandiProdotto(selezionato);
                    mostraSezioneEspansa(prodotto);
                }
            }
        });

        // Osservatore
        controller.registraOsservatore(this::mostraMarketplace);

        ObserverManager.registraOsservatore(this);
        mostraMarketplace(controller.ottieniListaProdotti()); // carica all’avvio


        // Caricamento iniziale
        controller.notificaOsservatori();
    }

    public void mostraMarketplace(List<Prodotto> prodotti) {
        modelloLista.clear();
        for (Prodotto p : prodotti) {
            modelloLista.addElement(p.getNome());
        }
    }

    private void mostraSezioneEspansa(Prodotto p) {
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

    @Override
    public void notifica(Prodotto prodotto, String evento) {
        if ("APPROVATO".equals(evento)) {
            SwingUtilities.invokeLater(() -> {
                // aggiorna la lista dei prodotti
                mostraMarketplace(controller.ottieniListaProdotti());
            });
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this);
    }



    public JButton getBtnIndietro() {
        return btnIndietro;
    }
}
