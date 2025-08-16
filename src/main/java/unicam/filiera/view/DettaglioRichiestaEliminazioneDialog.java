// src/main/java/unicam/filiera/view/DettaglioRichiestaEliminazioneDialog.java
package unicam.filiera.view;

import unicam.filiera.controller.GestoreRichiesteEliminazioneController;
import unicam.filiera.model.RichiestaEliminazioneProfilo;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class DettaglioRichiestaEliminazioneDialog extends JDialog {

    private final GestoreRichiesteEliminazioneController controller;
    private final int richiestaId;

    public interface OnDone {
        void refreshed(); // per ricaricare la lista in chiamante
    }

    public DettaglioRichiestaEliminazioneDialog(Window owner,
                                                GestoreRichiesteEliminazioneController controller,
                                                int richiestaId,
                                                OnDone callback) {
        super(owner, "Dettagli richiesta eliminazione", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.richiestaId = richiestaId;

        RichiestaEliminazioneProfilo r = controller.getDettaglio(richiestaId);
        if (r == null) {
            JOptionPane.showMessageDialog(owner, "Richiesta non trovata.", "Errore",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel(new GridLayout(0, 2, 8, 8));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        content.add(new JLabel("ID:"));
        content.add(new JLabel(String.valueOf(r.getId())));
        content.add(new JLabel("Username:"));
        content.add(new JLabel(r.getUsername()));
        content.add(new JLabel("Data richiesta:"));
        content.add(new JLabel(r.getDataRichiesta().format(fmt)));
        content.add(new JLabel("Stato:"));
        content.add(new JLabel(r.getStato().name()));

        add(content, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRifiuta = new JButton("Rifiuta richiesta");
        JButton btnAccetta = new JButton("Accetta richiesta");
        JButton btnChiudi = new JButton("Chiudi");
        buttons.add(btnRifiuta);
        buttons.add(btnAccetta);
        buttons.add(btnChiudi);
        add(buttons, BorderLayout.SOUTH);

        btnRifiuta.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Confermi il rifiuto della richiesta?",
                    "Conferma rifiuto",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean done = controller.rifiutaRichiesta(richiestaId);
                if (done) {
                    JOptionPane.showMessageDialog(this, "Richiesta rifiutata.");
                    if (callback != null) callback.refreshed();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Impossibile rifiutare la richiesta.",
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnAccetta.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Confermi l'eliminazione del profilo dell'utente '" + r.getUsername() + "'?",
                    "Conferma eliminazione",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean done = controller.approvaRichiesta(richiestaId);
                if (done) {
                    JOptionPane.showMessageDialog(this, "Profilo eliminato. Richiesta approvata.");
                    if (callback != null) callback.refreshed();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Operazione non riuscita (eliminazione utente o aggiornamento stato).",
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnChiudi.addActionListener(e -> dispose());

        setSize(480, 240);
        setLocationRelativeTo(owner);
    }
}
