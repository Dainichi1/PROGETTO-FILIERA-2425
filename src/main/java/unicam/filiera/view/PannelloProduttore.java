package unicam.filiera.view;

import unicam.filiera.controller.ProduttoreController;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.UtenteAutenticato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PannelloProduttore extends JPanel {

    private boolean formVisibile = false;
    private final JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
    private final List<File> certificatiSelezionati = new ArrayList<>();
    private final List<File> fotoSelezionate = new ArrayList<>();

    // Riferimento al controller (logica) del produttore
    private final ProduttoreController produttoreController;

    // Campi interfaccia
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

        formPanel.add(new JLabel()); // spazio vuoto
        formPanel.add(btnSalva);

        formPanel.setVisible(false);
        add(formPanel, BorderLayout.CENTER);

        // Tabella
        String[] colonne = {"Nome", "Quantità", "Prezzo", "Certificati", "Foto", "Stato", "Commento"};
        tableModel = new DefaultTableModel(colonne, 0);
        tabella = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabella);
        add(scrollPane, BorderLayout.EAST);

        // Listener per il toggle del form
        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto");
            revalidate();
            repaint();
        });

        // Seleziona certificati
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

        // Seleziona foto
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

        // Salva Prodotto (chiama il controller)
        btnSalva.addActionListener(e -> {
            int conferma = JOptionPane.showConfirmDialog(
                    this,
                    "Sei sicuro di voler inviare il prodotto al curatore per approvazione?",
                    "Conferma invio",
                    JOptionPane.YES_NO_OPTION
            );

            if (conferma != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                // Leggiamo i valori dalla GUI
                String nome = nomeField.getText().trim();
                String descrizione = descrizioneField.getText().trim();
                int quantita = Integer.parseInt(quantitaField.getText().trim());
                double prezzo = Double.parseDouble(prezzoField.getText().trim());

                // Invoca il metodo “creaNuovoProdotto” del controller
                boolean success = produttoreController.creaNuovoProdotto(
                        nome,
                        descrizione,
                        quantita,
                        prezzo,
                        certificatiSelezionati,
                        fotoSelezionate,
                        utente.getUsername()
                );

                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Prodotto inviato al curatore!",
                            "Successo",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Svuota il form
                    nomeField.setText("");
                    descrizioneField.setText("");
                    quantitaField.setText("");
                    prezzoField.setText("");
                    certificatiSelezionati.clear();
                    fotoSelezionate.clear();
                    labelCertificati.setText("Nessun file selezionato");
                    labelFoto.setText("Nessun file selezionato");

                    // Ricarichiamo la tabella
                    aggiornaTabella(utente.getUsername());
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Errore durante il salvataggio.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Quantità e Prezzo devono essere numeri validi.",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Errore: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Carica la tabella iniziale
        aggiornaTabella(utente.getUsername());
    }

    /**
     * Aggiorna la tabella leggendo i prodotti dal controller (invece che dal DAO).
     */
    private void aggiornaTabella(String username) {
        tableModel.setRowCount(0);
        List<Prodotto> prodotti = produttoreController.getProdottiCreatiDa(username);

        for (Prodotto p : prodotti) {
            tableModel.addRow(new Object[]{
                    p.getNome(),
                    p.getQuantita(),
                    p.getPrezzo(),
                    String.join(", ", p.getCertificati()),
                    String.join(", ", p.getFoto()),
                    p.getStato() != null ? p.getStato().name() : "N/D",
                    p.getCommento() != null ? p.getCommento() : ""
            });
        }
    }
}
