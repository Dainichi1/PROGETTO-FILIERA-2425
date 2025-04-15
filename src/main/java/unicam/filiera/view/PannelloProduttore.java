package unicam.filiera.view;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
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

    public PannelloProduttore(UtenteAutenticato utente) {
        setLayout(new BorderLayout());

        JLabel benvenuto = new JLabel("Benvenuto " + utente.getNome() + ", " + utente.getRuolo(), SwingConstants.CENTER);
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        JButton btnToggleForm = new JButton("Crea Prodotto");
        add(btnToggleForm, BorderLayout.SOUTH);



        // Campi form
        JTextField nomeField = new JTextField();
        JTextField descrizioneField = new JTextField();
        JTextField quantitaField = new JTextField();
        JTextField prezzoField = new JTextField();

        JButton btnCertificati = new JButton("Seleziona certificati");
        JButton btnFoto = new JButton("Seleziona foto");

        JLabel labelCertificati = new JLabel("Nessun file selezionato");
        JLabel labelFoto = new JLabel("Nessun file selezionato");

        JButton btnSalva = new JButton("Salva Prodotto");

        // Aggiungi componenti al form
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

        formPanel.setVisible(false); // inizialmente nascosto
        add(formPanel, BorderLayout.CENTER);

        // Tabella prodotti
        String[] colonne = {"Nome", "Quantità", "Prezzo", "Certificati", "Foto"};
        DefaultTableModel tableModel = new DefaultTableModel(colonne, 0);
        JTable tabella = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabella);
        add(scrollPane, BorderLayout.EAST); // oppure BorderLayout.SOUTH se preferisci

// Metodo per aggiornare la tabella
        Runnable aggiornaTabella = () -> {
            tableModel.setRowCount(0); // svuota la tabella
            ProdottoDAO dao = new ProdottoDAO();
            List<Prodotto> prodotti = dao.getProdottiByCreatore(utente.getUsername());
            for (Prodotto p : prodotti) {
                tableModel.addRow(new Object[]{
                        p.getNome(),
                        p.getQuantita(),
                        p.getPrezzo(),
                        String.join(", ", p.getCertificati()),
                        String.join(", ", p.getFoto())
                });
            }
        };


        // Toggle visibilità
        btnToggleForm.addActionListener(e -> {
            formVisibile = !formVisibile;
            formPanel.setVisible(formVisibile);
            btnToggleForm.setText(formVisibile ? "Chiudi form" : "Crea Prodotto");
            revalidate();
            repaint();
        });

        // FileChooser: Certificati
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

        // FileChooser: Foto
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

        // Salva Prodotto
        btnSalva.addActionListener(e -> {
            try {
                String nome = nomeField.getText().trim();
                String descrizione = descrizioneField.getText().trim();
                int quantita = Integer.parseInt(quantitaField.getText().trim());
                double prezzo = Double.parseDouble(prezzoField.getText().trim());

                if (nome.isEmpty() || descrizione.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Compila tutti i campi.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Prodotto prodotto = new Prodotto(
                        nome, descrizione, quantita, prezzo,
                        null, null, utente.getUsername()
                );

                ProdottoDAO dao = new ProdottoDAO();
                boolean success = dao.salvaProdotto(prodotto, certificatiSelezionati, fotoSelezionate);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Prodotto salvato!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                    nomeField.setText("");
                    descrizioneField.setText("");
                    quantitaField.setText("");
                    prezzoField.setText("");
                    certificatiSelezionati.clear();
                    fotoSelezionate.clear();
                    labelCertificati.setText("Nessun file selezionato");
                    labelFoto.setText("Nessun file selezionato");
                    aggiornaTabella.run(); // aggiorna la tabella dopo il salvataggio

                } else {
                    JOptionPane.showMessageDialog(this, "Errore durante il salvataggio.", "Errore", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantità e Prezzo devono essere numeri validi.", "Errore", JOptionPane.ERROR_MESSAGE);
            }

        });
        aggiornaTabella.run();

    }


}
