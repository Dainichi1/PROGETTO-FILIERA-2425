package unicam.filiera.view;

import unicam.filiera.controller.GestoreRichiesteEliminazioneController;
import unicam.filiera.controller.MappaController;
import unicam.filiera.model.MarkerData;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.repository.AddressInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import unicam.filiera.service.ProdottoTrasformatoServiceImpl;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.SupplyChainMapService;


public class PannelloGestore extends JPanel {

    private final UtenteAutenticato utente;
    private final GestoreRichiesteEliminazioneController richiesteController =
            new GestoreRichiesteEliminazioneController();

    public PannelloGestore(UtenteAutenticato utente) {
        super(new BorderLayout());
        this.utente = utente;

        // Header "Benvenuto ..."
        JLabel benv = new JLabel(
                "Benvenuto " + utente.getNome() + ", " + utente.getRuolo(),
                SwingConstants.CENTER
        );
        benv.setFont(new Font("Arial", Font.BOLD, 18));
        benv.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(benv, BorderLayout.NORTH);

        // Barra superiore comandi
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnContenuti = new JButton("Visualizza contenuto piattaforma");
        JButton btnRichieste = new JButton("Visualizza richieste di eliminazione in attesa");
        JButton btnShowMap = new JButton("Visualizza Mappa");
        JButton btnGestioneIndirizzi = new JButton("Gestione indirizzi mappa");
        topBar.add(btnShowMap);
        topBar.add(btnContenuti);
        topBar.add(btnRichieste);
        topBar.add(btnGestioneIndirizzi);
        add(topBar, BorderLayout.SOUTH);

        btnRichieste.addActionListener(e -> mostraRichiesteInAttesa());
        btnContenuti.addActionListener(e -> {
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Contenuto piattaforma", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setContentPane(new PannelloGestoreContenuti());
            dlg.setSize(1000, 600);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });
        btnShowMap.addActionListener(e -> {
            System.out.println("[MAP] Apertura mappa...");

            // 1) apri la mappa e carica i marker persistiti
            MappaFrame frame = new MappaFrame();
            var tuttiIMarker = unicam.filiera.repository.MarkerRepository.loadMarkers();
            System.out.println("[MAP] Marker persistenti caricati: " + tuttiIMarker.size());
            if (!tuttiIMarker.isEmpty()) {
                frame.setMarkersCustom(tuttiIMarker);
                var primo = tuttiIMarker.get(0);
                frame.setCenter(primo.lat(), primo.lon(), 12);
            }

            // 2) ricostruisci TUTTE le polilinee dalle filiere APPROVATE
            var svc = new SupplyChainMapService();
            var ptService = new ProdottoTrasformatoServiceImpl();
            var listaPT = ptService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO);
            System.out.println("[MAP] Prodotti trasformati approvati trovati: " + listaPT.size());

            for (var pt : listaPT) {
                System.out.println("[MAP] Ricostruisco filiera per: " + pt.getNome());
                var res = svc.buildFor(pt);
                frame.showSupplyChain(res.markers, res.path);
            }

            frame.setVisible(true);
        });



        // === GESTIONE INDIRIZZI PER MAPPA ===
        btnGestioneIndirizzi.addActionListener(e -> {
            SelezionaIndirizziDialog dlg = new SelezionaIndirizziDialog(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            List<AddressInfo> selezionati = dlg.getSelectedAddresses();

            if (!selezionati.isEmpty()) {
                var geocoder = new unicam.filiera.service.NominatimGeocodingService();
                var persistedMarkers = unicam.filiera.repository.MarkerRepository.loadMarkers();
                var nuoviMarker = new java.util.ArrayList<MarkerData>();

                // Validazione sequenziale: se un indirizzo è non valido o duplicato, STOP e ritorno al dialog
                for (AddressInfo info : selezionati) {
                    var result = geocoder.geocode(info.address());
                    if (result.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Indirizzo NON valido o non trovabile:\n" + info.address(),
                                "Errore indirizzo",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return; // torna alla selezione (nessun salvataggio)
                    }
                    var geo = result.get();
                    Color col = TYPE_COLORS.getOrDefault(info.sourceType(), Color.GRAY);
                    String label = info.address() + " (" + info.sourceType() + ")";
                    MarkerData nuovo = new MarkerData(geo.lat(), geo.lon(), label, col);

                    final double EPS = 1e-6;
                    boolean giaPresente = persistedMarkers.stream().anyMatch(md ->
                                    Math.abs(md.lat() - nuovo.lat()) < EPS && Math.abs(md.lon() - nuovo.lon()) < EPS
                            // in alternativa/aggiunta:
                            // || md.label().equalsIgnoreCase(nuovo.label())
                    );
                    if (giaPresente) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Questo indirizzo è già presente come marker sulla mappa:\n" + label,
                                "Indirizzo già presente",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return; // torna alla selezione (nessun salvataggio)
                    }

                    nuoviMarker.add(nuovo);
                }

                // Se arrivo qui, TUTTI sono validi e non duplicati: salvo SOLO i nuovi
                if (!nuoviMarker.isEmpty()) {
                    unicam.filiera.repository.MarkerRepository.saveMarkers(nuoviMarker);

                    // Ricarico tutti i marker persistenti e mostro la mappa aggiornata
                    var tuttiIMarker = unicam.filiera.repository.MarkerRepository.loadMarkers();
                    MappaFrame frame = new MappaFrame();
                    if (!tuttiIMarker.isEmpty()) {
                        frame.setMarkersCustom(tuttiIMarker);
                        var primo = tuttiIMarker.get(0);
                        frame.setCenter(primo.lat(), primo.lon(), 12);
                        var svc = new SupplyChainMapService();
                        var ptService = new ProdottoTrasformatoServiceImpl();
                        var listaPT = ptService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO);

                        for (var pt : listaPT) {
                            var res = svc.buildFor(pt);
                            // showSupplyChain NON sostituisce i marker, aggiunge solo la polilinea (e opzionalmente i marker di filiera)
                            frame.showSupplyChain(res.markers, res.path);
                        }
                    }
                    frame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Nessun indirizzo valido da visualizzare!", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // placeholder centro
        JPanel center = new JPanel(new GridBagLayout());
        center.add(new JLabel("Dashboard Gestore"));
        add(center, BorderLayout.CENTER);
    }

    private static final Map<String, Color> TYPE_COLORS = Map.of(
            "Prodotto", new Color(44, 177, 67),   // verde
            "Pacchetto", new Color(240, 189, 42),  // arancio
            "Prodotto Trasformato", new Color(51, 139, 201),  // blu
            "Fiera", new Color(212, 67, 41),   // rosso
            "Visita su Invito", new Color(143, 77, 204)   // viola
    );

    private void mostraRichiesteInAttesa() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Richieste di eliminazione in attesa", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Username", "Data richiesta", "Stato"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApri = new JButton("Apri dettagli");
        JButton btnChiudi = new JButton("Chiudi");
        south.add(btnApri);
        south.add(btnChiudi);
        dlg.add(south, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            var richieste = richiesteController.getRichiesteInAttesa();
            if (richieste.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Nessuna richiesta in attesa.",
                        "Richieste eliminazione", JOptionPane.INFORMATION_MESSAGE);
            }
            var fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (var r : richieste) {
                model.addRow(new Object[]{
                        r.getId(),
                        r.getUsername(),
                        r.getDataRichiesta() != null ? r.getDataRichiesta().format(fmt) : "",
                        r.getStato().name()
                });
            }
        };
        load.run();

        btnApri.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Seleziona una richiesta dalla lista.");
                return;
            }
            int richiestaId = (int) model.getValueAt(row, 0);
            var det = new DettaglioRichiestaEliminazioneDialog(
                    dlg,
                    richiesteController,
                    richiestaId,
                    load::run
            );
            det.setVisible(true);
        });

        btnChiudi.addActionListener(e -> dlg.dispose());

        dlg.setSize(800, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
