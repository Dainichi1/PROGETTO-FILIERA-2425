// -------- JdbcVisitaInvitoDAO.java --------
package unicam.filiera.dao;

import unicam.filiera.model.VisitaInvito;
import unicam.filiera.model.StatoEvento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcVisitaInvitoDAO implements VisitaInvitoDAO {
    private static JdbcVisitaInvitoDAO instance;

    private JdbcVisitaInvitoDAO() {}

    public static JdbcVisitaInvitoDAO getInstance() {
        if (instance == null) instance = new JdbcVisitaInvitoDAO();
        return instance;
    }

    @Override
    public boolean save(VisitaInvito v) {
        String sql = """
            INSERT INTO visite_invito
             (data_inizio, data_fine, prezzo, descrizione, indirizzo,
              organizzatore, numero_min_partecipanti, destinatari, stato)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(v.getDataInizio()));
            ps.setTimestamp(2, Timestamp.valueOf(v.getDataFine()));
            ps.setDouble(3, v.getPrezzo());
            ps.setString(4, v.getDescrizione());
            ps.setString(5, v.getIndirizzo());
            ps.setString(6, v.getOrganizzatore());
            ps.setInt(7, v.getNumeroMinPartecipanti());
            ps.setString(8, String.join(",", v.getDestinatari()));
            ps.setString(9, v.getStato().name());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) v.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(VisitaInvito v) {
        String sql = """
            UPDATE visite_invito
               SET data_inizio = ?, data_fine = ?, prezzo = ?, descrizione = ?,
                   indirizzo = ?, numero_min_partecipanti = ?, destinatari = ?, stato = ?
             WHERE id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(v.getDataInizio()));
            ps.setTimestamp(2, Timestamp.valueOf(v.getDataFine()));
            ps.setDouble(3, v.getPrezzo());
            ps.setString(4, v.getDescrizione());
            ps.setString(5, v.getIndirizzo());
            ps.setInt(6, v.getNumeroMinPartecipanti());
            ps.setString(7, String.join(",", v.getDestinatari()));
            ps.setString(8, v.getStato().name());
            ps.setLong(9, v.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(long id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM visite_invito WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // helper generico usato da findByOrganizzatore, findByStato e findAll
    private List<VisitaInvito> findBy(String sql, String param) {
        List<VisitaInvito> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildFromRs(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<VisitaInvito> findByOrganizzatore(String org) {
        return findBy("SELECT * FROM visite_invito WHERE organizzatore = ?", org);
    }

    @Override
    public List<VisitaInvito> findByStato(StatoEvento stato) {
        return findBy("SELECT * FROM visite_invito WHERE stato = ?", stato.name());
    }

    @Override
    public VisitaInvito findById(long id) {
        String sql = "SELECT * FROM visite_invito WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildFromRs(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<VisitaInvito> findAll() {
        return findBy("SELECT * FROM visite_invito", null);
    }

    private VisitaInvito buildFromRs(ResultSet rs) throws SQLException {
        List<String> dest = List.of(rs.getString("destinatari").split(","));
        return new VisitaInvito.Builder()
                .id(rs.getLong("id"))
                .dataInizio(rs.getTimestamp("data_inizio").toLocalDateTime())
                .dataFine(rs.getTimestamp("data_fine").toLocalDateTime())
                .prezzo(rs.getDouble("prezzo"))
                .descrizione(rs.getString("descrizione"))
                .indirizzo(rs.getString("indirizzo"))
                .organizzatore(rs.getString("organizzatore"))
                .numeroMinPartecipanti(rs.getInt("numero_min_partecipanti"))
                .destinatari(dest)
                .stato(StatoEvento.valueOf(rs.getString("stato")))
                .build();
    }
}
