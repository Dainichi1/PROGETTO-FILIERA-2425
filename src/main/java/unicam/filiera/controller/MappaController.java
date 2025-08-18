package unicam.filiera.controller;

import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.MarkerRepository;
import unicam.filiera.service.ProdottoTrasformatoServiceImpl;
import unicam.filiera.service.SupplyChainMapService;
import unicam.filiera.view.MappaFrame;

import javax.swing.*;
import java.util.List;

public class MappaController {
    private MappaFrame frame;

    /**
     * Mostra la mappa con tutti i marker e le linee di collegamento
     */
    public void mostra() {
        if (frame == null || !frame.isDisplayable()) {
            frame = new MappaFrame();

            // --- 1) Carica marker persistenti dal DB ---
            var tuttiIMarker = MarkerRepository.loadMarkers();
            if (!tuttiIMarker.isEmpty()) {
                frame.setMarkersCustom(tuttiIMarker);

                // centra la mappa sul primo marker
                var primo = tuttiIMarker.get(0);
                frame.setCenter(primo.lat(), primo.lon(), 12);
            }

            // --- 2) Ricostruisci le filiere (linee di collegamento) ---
            var svc = new SupplyChainMapService();
            var ptService = new ProdottoTrasformatoServiceImpl();
            var listaPT = ptService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO);

            for (var pt : listaPT) {
                var res = svc.buildFor(pt);
                frame.showSupplyChain(res.markers, res.path);
            }
        }

        frame.setVisible(true);
        frame.toFront();
    }

    /**
     * Pulisce i marker dalla mappa
     */
    public void pulisciMarker() {
        ensureOpen();
        SwingUtilities.invokeLater(frame::clearMarkers);
    }

    private void ensureOpen() {
        if (frame == null || !frame.isDisplayable()) mostra();
    }
}
