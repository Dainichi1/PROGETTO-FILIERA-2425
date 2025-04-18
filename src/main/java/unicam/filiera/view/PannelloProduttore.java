package unicam.filiera.view;

import unicam.filiera.controller.ProduttoreController;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.controller.ObserverManager;
import unicam.filiera.model.observer.OsservatoreProdotto;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.controller.ObserverManager;


public class PannelloProduttore extends JPanel implements OsservatoreProdotto {

    private final UtenteAutenticato utente;
    private boolean formVisibile = false;
    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private final List<File> certificatiSelezionati = new ArrayList<>();
    private final List<File> fotoSelezionate = new ArrayList<>();

    private final ProduttoreController produttoreController;

    private final JTextField nomeField = new JTextField();
    private final JTextField descrizioneField = new JTextField();
    private final JTextField quantitaField = new JTextField();
    private final JTextField prezzoField = new JTextField();

    private final JLabel labelCertificati = new JLabel("Nessun file selezionato");
    private final JLabel labelFoto = new JLabel("Nessun file selezionato");

    private final JButton btnToggleForm = new JButton("Crea Prodotto");
    private final JButton btnCertificati = new JButton("Seleziona certificati");
    private final JButton btnFoto = new JButton("Seleziona foto");
    private final JButton btnSalva = new JButton("Invia al Curatore");

    private final DefaultTableModel tableModel;
    private final JTable tabella;

    public PannelloProduttore(UtenteAutenticato utente) {
        super(new BorderLayout());

        this.utente = utente;
        this.produttoreController = new ProduttoreController();

        JLabel benvenuto = new JLabel("Benvenuto " + utente.getNome() + ", " + utente.getRuolo(), SwingConstants.CENTER);
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        add(btnToggleForm, BorderLayout.SOUTH);

        // Pannello form
        formPanel.add(new JLabel("Nome prodotto:"));
        formPanel.add(nomeField);

        formPanel.add(new JLabel("Descrizione:"));
        formPanel.add(descrizioneField);

        formPanel.add(new JLabel("Quantità:"));
        formPanel.add(quantitaField);

        formPanel.add(new JLabel("Prezzo:"));
        formPanel.add(prezzoField);

        formPanel.add(btnCertificati);
        formPanel.add(labelCertificati);

        formPanel.add(btnFoto);
        formPanel.add(labelFoto);

        formPanel.add(new JLabel());
        formPanel.add(btnSalva);

        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);

        // Tabella
        String[] colonne = {"Nome", "Descrizione", "Quantità", "Prezzo", "Certificati", "Foto", "Stato", "Commento"};
        tableModel = new DefaultTableModel(colonne, 0);
        tabella = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabella);
        add(scrollPane, BorderLayout.EAST);

        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto");
            revalidate();
            repaint();
        });

        btnCertificati.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                certificatiSelezionati.clear();
                certificatiSelezionati.addAll(List.of(chooser.getSelectedFiles()));
                labelCertificati.setText(certificatiSelezionati.size() + " file selezionati");
            }
        });

        btnFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                fotoSelezionate.clear();
                fotoSelezionate.addAll(List.of(chooser.getSelectedFiles()));
                labelFoto.setText(fotoSelezionate.size() + " file selezionati");
            }
        });

        btnSalva.addActionListener(e -> {
            int conferma = JOptionPane.showConfirmDialog(
                    this,
                    "Sei sicuro di voler inviare il prodotto al curatore per approvazione?",
                    "Conferma invio",
                    JOptionPane.YES_NO_OPTION
            );

            if (conferma != JOptionPane.YES_OPTION) return;

            try {
                String nome = nomeField.getText().trim();
                String descrizione = descrizioneField.getText().trim();
                String qStr = quantitaField.getText().trim();
                String pStr = prezzoField.getText().trim();

                // ✅ Validazioni campi obbligatori
                if (nome.isEmpty() || descrizione.isEmpty() || qStr.isEmpty() || pStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Compila tutti i campi!", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ✅ Validazioni certificati / foto
                if (certificatiSelezionati.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Seleziona almeno un certificato!", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (fotoSelezionate.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Seleziona almeno una foto!", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantita = Integer.parseInt(qStr);
                double prezzo = Double.parseDouble(pStr);

                // 1. Crea l'oggetto prodotto
                Prodotto prodotto = produttoreController.creaNuovoProdotto(
                        nome, descrizione, quantita, prezzo,
                        utente.getUsername()
                );

                // 2. Salva i dettagli nel DB (prima!)
                boolean dettagliOk = produttoreController.inviaDatiProdotto(prodotto);

                // 3. Salva i file (dopo che il prodotto esiste nel DB)
                boolean fileOk = produttoreController.uploadFile(certificatiSelezionati, fotoSelezionate, prodotto);

                // 4. Aggiorna stato
                boolean statoOk = produttoreController.inoltraModulo(prodotto);

                // 5. Invia nel marketplace
                boolean finaleOk = produttoreController.inviaNuovoProdotto(prodotto);


                if (fileOk && statoOk && dettagliOk && finaleOk) {
                    JOptionPane.showMessageDialog(this, "Prodotto inviato al curatore!", "Successo", JOptionPane.INFORMATION_MESSAGE);

                    // Reset campi
                    nomeField.setText("");
                    descrizioneField.setText("");
                    quantitaField.setText("");
                    prezzoField.setText("");
                    certificatiSelezionati.clear();
                    fotoSelezionate.clear();
                    labelCertificati.setText("Nessun file selezionato");
                    labelFoto.setText("Nessun file selezionato");

                    aggiornaTabella(utente.getUsername());
                } else {
                    JOptionPane.showMessageDialog(this, "Errore durante il salvataggio.", "Errore", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantità e Prezzo devono essere numeri validi.", "Errore", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Carica tabella iniziale
        aggiornaTabella(utente.getUsername());

        ObserverManager.registraOsservatore(this);

    }

    private void aggiornaTabella(String username) {
        tableModel.setRowCount(0);
        List<Prodotto> prodotti = produttoreController.getProdottiCreatiDa(username);

        for (Prodotto p : prodotti) {
            tableModel.addRow(new Object[]{
                    p.getNome(),
                    p.getDescrizione(),
                    p.getQuantita(),
                    p.getPrezzo(),
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato() != null ? p.getStato().name() : "N/D",
                    p.getCommento() != null ? p.getCommento() : ""
            });

        }
    }

    @Override
    public void notifica(Prodotto prodotto, String evento) {
        if (!prodotto.getCreatoDa().equalsIgnoreCase(utente.getUsername())) return;

        SwingUtilities.invokeLater(() -> {
            if ("APPROVATO".equals(evento)) {
                JOptionPane.showMessageDialog(this,
                        "✔ Il tuo prodotto \"" + prodotto.getNome() + "\" è stato APPROVATO!",
                        "Prodotto approvato",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if ("RIFIUTATO".equals(evento)) {
                String messaggio = "❌ Il tuo prodotto \"" + prodotto.getNome() + "\" è stato RIFIUTATO.";
                if (prodotto.getCommento() != null && !prodotto.getCommento().isBlank()) {
                    messaggio += "\nCommento del curatore: " + prodotto.getCommento();
                }
                JOptionPane.showMessageDialog(this,
                        messaggio,
                        "Prodotto rifiutato",
                        JOptionPane.WARNING_MESSAGE);
            }

            // Aggiorna la tabella ogni volta che cambia qualcosa
            aggiornaTabella(utente.getUsername());
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this);
    }


}
