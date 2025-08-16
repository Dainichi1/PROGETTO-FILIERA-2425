package unicam.filiera.view;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;


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





}

