package unicam.filiera.service;

import unicam.filiera.model.*;
import unicam.filiera.repository.ProductRepository;
import unicam.filiera.repository.MarkerRepository;

import java.awt.Color;
import java.util.*;

public class SupplyChainMapService {
    private final NominatimGeocodingService geocoder = new NominatimGeocodingService();
    private static final double EPS = 1e-6;

    public static class ChainResult {
        public final List<MarkerData> markers;
        public final List<org.openstreetmap.gui.jmapviewer.Coordinate> path;

        public ChainResult(List<MarkerData> markers,
                           List<org.openstreetmap.gui.jmapviewer.Coordinate> path) {
            this.markers = markers;
            this.path = path;
        }
    }

    public ChainResult buildFor(ProdottoTrasformato pt) {
        List<MarkerData> markers = new ArrayList<>();
        List<org.openstreetmap.gui.jmapviewer.Coordinate> path = new ArrayList<>();

        // Marker già salvati
        List<MarkerData> allMarkers = MarkerRepository.loadMarkers();

        int step = 1;
        for (FaseProduzione f : pt.getFasiProduzione()) {
            String nomeProdBase = f.getProdottoOrigine();
            org.openstreetmap.gui.jmapviewer.Coordinate coord = null;
            String label = step + ". " + nomeProdBase + " (fase: " + f.getDescrizioneFase() + ")";

            System.out.println("[SC] Elaboro fase: " + nomeProdBase);

            // a) Cerca marker persistente
            Optional<MarkerData> m = allMarkers.stream()
                    .filter(md -> md.label().toLowerCase().contains(nomeProdBase.toLowerCase()))
                    .findFirst();
            if (m.isPresent()) {
                coord = new org.openstreetmap.gui.jmapviewer.Coordinate(m.get().lat(), m.get().lon());
                System.out.println("[SC]   trovato marker già persistente: " + m.get().label());
            } else {
                // b) Cerca indirizzo nel DB
                String addr = ProductRepository.findAddressByProductName(nomeProdBase);
                System.out.println("[SC]   indirizzo DB: " + addr);

                if (addr != null && !addr.isBlank()) {
                    var geo = geocoder.geocode(addr);
                    System.out.println("[SC]   geocode riuscito? " + geo.isPresent());
                    if (geo.isPresent()) {
                        coord = new org.openstreetmap.gui.jmapviewer.Coordinate(geo.get().lat(), geo.get().lon());
                        MarkerData nuovo = new MarkerData(
                                geo.get().lat(), geo.get().lon(), label, new Color(120, 120, 120));

                        markers.add(nuovo);

                        // Persisto il nuovo marker se non già presente
                        if (!alreadyPresent(allMarkers, nuovo)) {
                            allMarkers.add(nuovo);
                            MarkerRepository.saveMarkers(allMarkers);
                            System.out.println("[SC]   nuovo marker salvato su repository: " + label);
                        }
                    }
                }
            }

            if (coord != null) {
                path.add(coord);
                step++;
            } else {
                System.out.println("[SC]   coordinate NON trovate per fase: " + nomeProdBase);
            }
        }

        // === Indirizzo finale PT
        org.openstreetmap.gui.jmapviewer.Coordinate finale = null;
        Optional<MarkerData> mf = allMarkers.stream()
                .filter(md -> pt.getIndirizzo() != null &&
                        md.label().toLowerCase().contains(pt.getIndirizzo().toLowerCase()))
                .findFirst();
        if (mf.isPresent()) {
            finale = new org.openstreetmap.gui.jmapviewer.Coordinate(mf.get().lat(), mf.get().lon());
            System.out.println("[SC] Finale già presente come marker: " + mf.get().label());
        } else if (pt.getIndirizzo() != null && !pt.getIndirizzo().isBlank()) {
            var geo = geocoder.geocode(pt.getIndirizzo());
            System.out.println("[SC] Geocode finale riuscito? " + geo.isPresent());
            if (geo.isPresent()) {
                finale = new org.openstreetmap.gui.jmapviewer.Coordinate(geo.get().lat(), geo.get().lon());
                MarkerData finaleMarker = new MarkerData(
                        geo.get().lat(), geo.get().lon(),
                        "Finale: " + pt.getNome() + " (Prodotto Trasformato)", new Color(51, 139, 201));

                markers.add(finaleMarker);

                if (!alreadyPresent(allMarkers, finaleMarker)) {
                    allMarkers.add(finaleMarker);
                    MarkerRepository.saveMarkers(allMarkers);
                    System.out.println("[SC]   finale salvato su repository: " + finaleMarker.label());
                }
            }
        }
        if (finale != null) path.add(finale);

        return new ChainResult(markers, path);
    }

    private static boolean alreadyPresent(List<MarkerData> list, MarkerData m) {
        return list.stream().anyMatch(md ->
                Math.abs(md.lat() - m.lat()) < EPS &&
                        Math.abs(md.lon() - m.lon()) < EPS &&
                        md.label().equalsIgnoreCase(m.label())
        );
    }
}
