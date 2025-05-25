package unicam.filiera.dao;

import unicam.filiera.model.PrenotazioneFiera;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcPrenotazioneFieraDAO implements PrenotazioneFieraDAO {

    @Override
    public boolean save(PrenotazioneFiera p) {
        String sql = """
            INSERT INTO prenotazioni_fiere
            (id_fiera, username_acquirente, numero_persone, data_prenotazione)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getIdFiera());
            ps.setString(2, p.getUsernameAcquirente());
            ps.setInt(3, p.getNumeroPersone());
            ps.setTimestamp(4, Timestamp.valueOf(
                    p.getDataPrenotazione() != null ? p.getDataPrenotazione() : LocalDateTime.now()
            ));
            int res = ps.executeUpdate();
            if (res > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<PrenotazioneFiera> findByUsername(String username) {
        List<PrenotazioneFiera> out = new ArrayList<>();
        String sql = "SELECT * FROM prenotazioni_fiere WHERE username_acquirente = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(buildFromRs(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public List<PrenotazioneFiera> findByFiera(long idFiera) {
        List<PrenotazioneFiera> out = new ArrayList<>();
        String sql = "SELECT * FROM prenotazioni_fiere WHERE id_fiera = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idFiera);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(buildFromRs(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    private PrenotazioneFiera buildFromRs(ResultSet rs) throws SQLException {
        PrenotazioneFiera p = new PrenotazioneFiera();
        p.setId(rs.getLong("id"));
        p.setIdFiera(rs.getLong("id_fiera"));
        p.setUsernameAcquirente(rs.getString("username_acquirente"));
        p.setNumeroPersone(rs.getInt("numero_persone"));
        p.setDataPrenotazione(rs.getTimestamp("data_prenotazione").toLocalDateTime());
        return p;
    }

    @Override
    public boolean existsByFieraAndAcquirente(long idFiera, String usernameAcquirente) {
        String sql = "SELECT COUNT(*) FROM prenotazioni_fiere WHERE id_fiera = ? AND username_acquirente = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idFiera);
            ps.setString(2, usernameAcquirente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(long idPrenotazione) {
        String sql = "DELETE FROM prenotazioni_fiere WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPrenotazione);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public PrenotazioneFiera findById(long idPrenotazione) {
        String sql = "SELECT * FROM prenotazioni_fiere WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPrenotazione);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildFromRs(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
