package unicam.filiera.view;

import unicam.filiera.controller.CuratoreController;
import unicam.filiera.controller.ObserverManager;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.observer.OsservatorePacchetto;
import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.model.observer.PacchettoNotifier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class PannelloCuratore extends JPanel
        implements OsservatoreProdotto, OsservatorePacchetto {

    /* ------------------------------------------------------------------ */
    /*  CAMPI                                                             */
    /* ------------------------------------------------------------------ */
    private final JTable            tabella;
    private final DefaultTableModel model;
    private final JScrollPane       scrollPane;
    private final JButton           toggleButton;
    private final CuratoreController controller = new CuratoreController();

    /* ------------------------------------------------------------------ */
    /*  COSTRUTTORE                                                       */
    /* ------------------------------------------------------------------ */
    public PannelloCuratore(UtenteAutenticato utente) {
        super(new BorderLayout());

        /* Header */
        JLabel benvenuto = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER);
        benvenuto.setFont(new Font("Arial", Font.BOLD, 18));
        add(benvenuto, BorderLayout.NORTH);

        /* Tabella */
        String[] col = {"Nome","Descrizione","Quantità","Prezzo","Indirizzo",
                "Creato da","Certificati","Foto",
                "Accetta","Rifiuta","Commento"};
        model   = new DefaultTableModel(col,0){
            @Override public boolean isCellEditable(int r,int c){
                return c==8||c==9||c==10;
            }
        };
        tabella = new JTable(model);
        tabella.setRowHeight(40);
        tabella.getColumn("Accetta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Accetta").setCellEditor  (new ComponentCellEditor());
        tabella.getColumn("Rifiuta").setCellRenderer(new ComponentCellRenderer());
        tabella.getColumn("Rifiuta").setCellEditor  (new ComponentCellEditor());
        tabella.getColumn("Commento").setCellRenderer(new ComponentCellRenderer());

        scrollPane = new JScrollPane(tabella);
        scrollPane.setVisible(false);
        add(scrollPane, BorderLayout.CENTER);

        /* Toggle */
        toggleButton = new JButton("Visualizza elementi da approvare");
        toggleButton.addActionListener(e -> {
            if (!scrollPane.isVisible()) caricaElementiInAttesa();
            scrollPane.setVisible(!scrollPane.isVisible());
            toggleButton.setText(scrollPane.isVisible()
                    ? "Nascondi lista elementi"
                    : "Visualizza elementi da approvare");
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JFrame f) f.pack();
            revalidate(); repaint();
        });
        add(toggleButton, BorderLayout.SOUTH);

        /* Observer */
        ObserverManager.registraOsservatore(this);          // prodotti
        PacchettoNotifier.getInstance().registraOsservatore(this); // pacchetti

        caricaElementiInAttesa();
    }

    /* ------------------------------------------------------------------ */
    /*  CALLBACK – PRODOTTI                                               */
    /* ------------------------------------------------------------------ */
    @Override
    public void notifica(Prodotto prodotto, String evento) {
        if ("NUOVO_PRODOTTO".equals(evento)) {
            SwingUtilities.invokeLater(this::caricaElementiInAttesa);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  CALLBACK – PACCHETTI                                              */
    /* ------------------------------------------------------------------ */
    @Override
    public void notifica(Pacchetto pacchetto, String evento) {
        if ("NUOVO_PACCHETTO".equals(evento)) {
            SwingUtilities.invokeLater(this::caricaElementiInAttesa);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  CARICAMENTO DATI                                                  */
    /* ------------------------------------------------------------------ */
    private void caricaElementiInAttesa() {
        model.setRowCount(0);

        for (Prodotto p : controller.getProdottiDaApprovare())
            aggiungiRigaGenerica(p);

        for (Pacchetto k : controller.getPacchettiDaApprovare())
            aggiungiRigaGenerica(k);
    }

    /* ------------------------------------------------------------------ */
    /*  AGGIUNTA RIGA GENERICA                                            */
    /* ------------------------------------------------------------------ */
    private void aggiungiRigaGenerica(Object elemento) {
        JButton btnAccetta = new JButton("✔");
        JButton btnRifiuta = new JButton("✖");

        String nomeVisualizzato, descrizione, indirizzo, creatoDa;
        double prezzo;
        List<String> certificati, foto;
        int quantita;
        boolean isPacchetto;

        if (elemento instanceof Prodotto p) {
            isPacchetto = false;
            nomeVisualizzato = p.getNome();
            descrizione      = p.getDescrizione();
            quantita         = p.getQuantita();
            prezzo           = p.getPrezzo();
            indirizzo        = p.getIndirizzo();
            creatoDa         = p.getCreatoDa();
            certificati      = p.getCertificati();
            foto             = p.getFoto();
        } else if (elemento instanceof Pacchetto k) {
            isPacchetto      = true;
            nomeVisualizzato = "[PACCHETTO] " + k.getNome();
            descrizione      = k.getDescrizione();
            quantita         = k.getProdotti().size();
            prezzo           = k.getPrezzoTotale();
            indirizzo        = k.getIndirizzo();
            creatoDa         = k.getCreatoDa();
            certificati      = k.getCertificati();
            foto             = k.getFoto();
        } else return;

        model.addRow(new Object[]{
                nomeVisualizzato, descrizione, quantita, prezzo,
                indirizzo, creatoDa,
                String.join(", ", certificati),
                String.join(", ", foto),
                btnAccetta, btnRifiuta, ""
        });
        int row = model.getRowCount()-1;

        /* Accetta */
        btnAccetta.addActionListener(e -> {
            if (tabella.isEditing()) tabella.getCellEditor().stopCellEditing();
            boolean ok = isPacchetto
                    ? controller.approvaPacchetto((Pacchetto) elemento)
                    : controller.approvaProdotto((Prodotto) elemento);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        (isPacchetto? "Pacchetto":"Prodotto") + " approvato!");
                model.removeRow(row);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Errore durante l'approvazione!",
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        /* Rifiuta */
        btnRifiuta.addActionListener(e -> {
            if (tabella.isEditing()) tabella.getCellEditor().stopCellEditing();
            String commento = (String) model.getValueAt(row, 10);
            boolean ok = isPacchetto
                    ? controller.rifiutaPacchetto((Pacchetto) elemento, commento)
                    : controller.rifiutaProdotto((Prodotto)  elemento, commento);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        (isPacchetto? "Pacchetto":"Prodotto") + " rifiutato!");
                caricaElementiInAttesa();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Errore durante il rifiuto!",
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*  CELL EDITOR DUMMY                                                 */
    /* ------------------------------------------------------------------ */
    private static class ComponentCellEditor extends AbstractCellEditor
            implements TableCellEditor {
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean s, int r, int c) { return (Component) v; }
        @Override public Object getCellEditorValue() { return null; }
    }

    /* ------------------------------------------------------------------ */
    /*  UN-REGISTER                                                       */
    /* ------------------------------------------------------------------ */
    @Override
    public void removeNotify() {
        super.removeNotify();
        ObserverManager.rimuoviOsservatore(this); // prodotti
        PacchettoNotifier.getInstance().rimuoviOsservatore(this); // pacchetti
    }
}
