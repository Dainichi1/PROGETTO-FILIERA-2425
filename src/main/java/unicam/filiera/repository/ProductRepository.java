package unicam.filiera.repository;

import java.sql.*;

public class ProductRepository {
    public static String findAddressByProductName(String nome) {
        // Primo tentativo: indirizzo nella tabella prodotti
        String sql = "SELECT indirizzo FROM prodotti WHERE LOWER(TRIM(nome)) = LOWER(TRIM(?)) LIMIT 1";
        try (Connection c = unicam.filiera.dao.DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("indirizzo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
