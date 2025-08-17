package unicam.filiera.repository;

import java.sql.*;
import java.util.*;

public class AddressRepository {
    public static List<AddressInfo> findAllDistinctAddressesWithSource() {
        List<AddressInfo> addresses = new ArrayList<>();
        // tabella, colonna PK, colonna indirizzo, tipo da visualizzare
        Object[][] queries = {
                {"prodotti", "id", "indirizzo", "Prodotto"},
                {"pacchetti", "id", "indirizzo", "Pacchetto"},
                {"prodotti_trasformati", "id", "indirizzo", "Prodotto Trasformato"},
                {"fiere", "id", "indirizzo", "Fiera"},
                {"visite_invito", "id", "indirizzo", "Visita su Invito"}
        };

        try (Connection conn = unicam.filiera.dao.DatabaseManager.getConnection()) {
            for (Object[] q : queries) {
                String table = (String) q[0], pk = (String) q[1], addrCol = (String) q[2], type = (String) q[3];
                String sql = "SELECT " + pk + ", " + addrCol + " FROM " + table;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String addr = rs.getString(addrCol);
                            long id = rs.getLong(pk);
                            if (addr != null && !addr.isBlank()) {
                                addresses.add(new AddressInfo(addr.trim(), type, id));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Facoltativo: elimina duplicati (stessa stringa indirizzo/provenienza)
        // Usa un Set se vuoi solo indirizzi unici, oppure restituisci tutti se vuoi poter distinguere i marker duplicati!
        return addresses;
    }
}
