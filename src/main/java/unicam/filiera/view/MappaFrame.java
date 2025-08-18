package unicam.filiera.view;

import org.openstreetmap.gui.jmapviewer.*;

import unicam.filiera.model.MarkerData;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import unicam.filiera.repository.MarkerRepository;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MappaFrame extends JFrame {
    private final JMapViewer map = new JMapViewer();

    public MappaFrame() {
        super("Mappa");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        add(map, BorderLayout.CENTER);

        // --- Carica e mostra i marker persistenti all'avvio ---
        java.util.List<MarkerData> persistedMarkers = MarkerRepository.loadMarkers();
        if (!persistedMarkers.isEmpty()) {
            setMarkersCustom(persistedMarkers);
            // Centra la mappa sul primo marker
            MarkerData first = persistedMarkers.get(0);
            setCenter(first.lat(), first.lon(), 12);
        }
    }


    public void clearMarkers() {
        map.removeAllMapMarkers();
        map.repaint();
    }

    public void addMarker(double lat, double lon, String label, Color backColor) {
        MapMarkerDot m = new MapMarkerDot(lat, lon);
        if (label != null) m.setName(label);
        if (backColor != null) m.setBackColor(backColor);
        map.addMapMarker(m);
    }

    public void setMarkersCustom(List<MarkerData> markers) {
        map.removeAllMapMarkers();
        for (MarkerData md : markers) {
            MapMarkerDot m = new MapMarkerDot(md.lat(), md.lon());
            m.setName(md.label());
            m.setBackColor(md.color());
            map.addMapMarker(m);
        }
        fitToMarkers();
    }

    public void fitToMarkers() {
        java.util.List<org.openstreetmap.gui.jmapviewer.interfaces.MapMarker> list = map.getMapMarkerList();
        if (list != null && !list.isEmpty()) {
            map.setDisplayToFitMapMarkers();
        }
    }

    public void setCenter(double lat, double lon, int zoom) {
        map.setDisplayPosition(new Coordinate(lat, lon), zoom);

    }

    public void setPathLine(List<Coordinate> coords, Color color) {
        if (coords == null || coords.size() < 2) return;


        // Disegna segmento per segmento per evitare la chiusura dell’ultimo sul primo
        for (int i = 0; i < coords.size() - 1; i++) {
            List<Coordinate> seg = java.util.Arrays.asList(
                    coords.get(i),
                    coords.get(i + 1),
                    coords.get(i + 1) // punto duplicato per creare un "triangolo" degenerato
            );
            MapPolygonImpl poly = new MapPolygonImpl(seg);
            poly.setColor(color);                         // colore del tratto
            poly.setBackColor(new Color(0, 0, 0, 0));     // nessun riempimento
            poly.setStroke(new BasicStroke(3.5f));

            map.addMapPolygon(poly);
        }
        map.repaint();
    }


    public void showSupplyChain(List<MarkerData> markers, List<Coordinate> path) {
        if (markers != null) {
            for (MarkerData md : markers) {
                addMarker(md.lat(), md.lon(), md.label(), md.color());
            }
        }
        setPathLine(path, new Color(51,139,201));
        if (path != null && !path.isEmpty()) {
            Coordinate first = path.get(0);
            setCenter(first.getLat(), first.getLon(), 12);
        }
    }




}

