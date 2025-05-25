package unicam.filiera.dao;

import unicam.filiera.model.PrenotazioneVisita;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcPrenotazioneVisitaDAO implements PrenotazioneVisitaDAO {

    private static JdbcPrenotazioneVisitaDAO instance;

    public static JdbcPrenotazioneVisitaDAO getInstance() {
        if (instance == null) instance = new JdbcPrenotazioneVisitaDAO();
        return instance;
    }

    private JdbcPrenotazioneVisitaDAO() {
    }

    @Override
    public boolean save(PrenotazioneVisita p) {
        String sql = "INSERT INTO prenotazioni_visite (id_visita, username_venditore, numero_persone, data_prenotazione) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getIdVisita());
            ps.setString(2, p.getUsernameVenditore());
            ps.setInt(3, p.getNumeroPersone());
            ps.setTimestamp(4, Timestamp.valueOf(p.getDataPrenotazione()));
            int affected = ps.executeUpdate();
            if (affected == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<PrenotazioneVisita> findByUsername(String usernameVenditore) {
        List<PrenotazioneVisita> out = new ArrayList<>();
        String sql = "SELECT * FROM prenotazioni_visite WHERE username_venditore = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usernameVenditore);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) out.add(fromResultSet(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    @Override
    public List<PrenotazioneVisita> findByVisita(long idVisita) {
        List<PrenotazioneVisita> out = new ArrayList<>();
        String sql = "SELECT * FROM prenotazioni_visite WHERE id_visita = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idVisita);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) out.add(fromResultSet(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    @Override
    public boolean existsByVisitaAndVenditore(long idVisita, String usernameVenditore) {
        String sql = "SELECT COUNT(*) FROM prenotazioni_visite WHERE id_visita = ? AND username_venditore = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idVisita);
            ps.setString(2, usernameVenditore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(long idPrenotazione) {
        String sql = "DELETE FROM prenotazioni_visite WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idPrenotazione);
            return ps.executeUpdate() == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public PrenotazioneVisita findById(long idPrenotazione) {
        String sql = "SELECT * FROM prenotazioni_visite WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idPrenotazione);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return fromResultSet(rs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private PrenotazioneVisita fromResultSet(ResultSet rs) throws SQLException {
        PrenotazioneVisita p = new PrenotazioneVisita();
        p.setId(rs.getLong("id"));
        p.setIdVisita(rs.getLong("id_visita"));
        p.setUsernameVenditore(rs.getString("username_venditore"));
        p.setNumeroPersone(rs.getInt("numero_persone"));
        p.setDataPrenotazione(rs.getTimestamp("data_prenotazione").toLocalDateTime());
        return p;
    }
}
