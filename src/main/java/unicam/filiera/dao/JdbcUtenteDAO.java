package unicam.filiera.dao;

import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtenteDAO implements UtenteDAO {

    private static JdbcUtenteDAO instance;

    private JdbcUtenteDAO() {
        DatabaseManager.initDatabase();
    }

    public static JdbcUtenteDAO getInstance() {
        if (instance == null) {
            instance = new JdbcUtenteDAO();
        }
        return instance;
    }

    @Override
    public boolean registraUtente(UtenteAutenticato utente) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // --- controllo username ---
            String checkUsername = "SELECT COUNT(*) FROM utenti WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkUsername)) {
                ps.setString(1, utente.getUsername());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
            // --- controllo persona ---
            String checkPersona = "SELECT COUNT(*) FROM utenti WHERE nome = ? AND cognome = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkPersona)) {
                ps.setString(1, utente.getNome());
                ps.setString(2, utente.getCognome());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
            // --- inserimento ---
            String insert = "INSERT INTO utenti (username, password, nome, cognome, ruolo) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, utente.getUsername());
                ps.setString(2, utente.getPassword());
                ps.setString(3, utente.getNome());
                ps.setString(4, utente.getCognome());
                ps.setString(5, utente.getRuolo().name());
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UtenteAutenticato login(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM utenti WHERE username = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new UtenteAutenticato(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            Ruolo.valueOf(rs.getString("ruolo"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UtenteAutenticato> findAll() {
        List<UtenteAutenticato> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM utenti")) {
            while (rs.next()) {
                list.add(new UtenteAutenticato(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        Ruolo.valueOf(rs.getString("ruolo"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean existsUsername(String username) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM utenti WHERE username = ?")) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsPersona(String nome, String cognome) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM utenti WHERE nome = ? AND cognome = ?")) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<UtenteAutenticato> findByRuoli(List<Ruolo> ruoli) {
        if (ruoli.isEmpty()) return List.of();
        // costruisco in modo dinamico la clausola IN (?,?,...)
        String placeholders = String.join(",", ruoli.stream().map(r -> "?").toList());
        String sql = "SELECT * FROM utenti WHERE ruolo IN (" + placeholders + ")";
        List<UtenteAutenticato> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ruoli.size(); i++) {
                ps.setString(i + 1, ruoli.get(i).name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UtenteAutenticato u = new UtenteAutenticato(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            Ruolo.valueOf(rs.getString("ruolo"))
                    );
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
