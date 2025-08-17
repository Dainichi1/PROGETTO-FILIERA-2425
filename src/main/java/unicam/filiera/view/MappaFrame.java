package unicam.filiera.view;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
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


}

