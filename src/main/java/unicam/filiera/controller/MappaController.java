package unicam.filiera.controller;

import unicam.filiera.view.MappaFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class MappaController {
    private MappaFrame frame;

    public void mostra() {
        if (frame == null || !frame.isDisplayable()) {
            frame = new MappaFrame();

        }
        frame.setVisible(true);
        frame.toFront();
    }

    public void pulisciMarker() {
        ensureOpen();
        SwingUtilities.invokeLater(frame::clearMarkers);
    }





    private void ensureOpen() {
        if (frame == null || !frame.isDisplayable()) mostra();
    }
}
