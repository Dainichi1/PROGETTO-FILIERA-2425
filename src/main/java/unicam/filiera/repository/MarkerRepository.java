package unicam.filiera.repository;

import unicam.filiera.model.MarkerData;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarkerRepository {
    public static void saveMarkers(List<MarkerData> markers) {
        try (Connection conn = unicam.filiera.dao.DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO markers (lat, lon, label, color) VALUES (?, ?, ?, ?)")) {
                for (MarkerData m : markers) {
                    ps.setDouble(1, m.lat());
                    ps.setDouble(2, m.lon());
                    ps.setString(3, m.label());
                    ps.setString(4, String.format("#%06x", m.color().getRGB() & 0xFFFFFF));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<MarkerData> loadMarkers() {
        List<MarkerData> list = new ArrayList<>();
        try (Connection conn = unicam.filiera.dao.DatabaseManager.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT lat, lon, label, color FROM markers")) {
                while (rs.next()) {
                    double lat = rs.getDouble("lat");
                    double lon = rs.getDouble("lon");
                    String label = rs.getString("label");
                    Color color = Color.decode(rs.getString("color"));
                    list.add(new MarkerData(lat, lon, label, color));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
