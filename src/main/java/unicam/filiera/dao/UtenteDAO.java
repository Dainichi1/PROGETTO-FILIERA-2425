package unicam.filiera.dao;

import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO {

    private static UtenteDAO instance;

    private UtenteDAO() {
        DatabaseManager.initDatabase(); // inizializza tabella
    }

    public static UtenteDAO getInstance() {
        if (instance == null) {
            instance = new UtenteDAO();
        }
        return instance;
    }

    // REGISTRA UTENTE
    public boolean registraUtente(UtenteAutenticato utente) {
        try (Connection conn = DatabaseManager.getConnection()) {

            // Controllo: username già registrato
            String checkUsernameSql = "SELECT COUNT(*) FROM utenti WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql)) {
                checkStmt.setString(1, utente.getUsername());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Username già registrato");
                    return false;
                }
            }

            // 🔐 Controllo: nome + cognome già registrati
            String checkPersonaSql = "SELECT COUNT(*) FROM utenti WHERE nome = ? AND cognome = ?";
            try (PreparedStatement personaStmt = conn.prepareStatement(checkPersonaSql)) {
                personaStmt.setString(1, utente.getNome());
                personaStmt.setString(2, utente.getCognome());
                ResultSet rs = personaStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Questa persona è già registrata con un altro ruolo");
                    return false;
                }
            }

            // Inserimento
            String insertSql = "INSERT INTO utenti (username, password, nome, cognome, ruolo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, utente.getUsername());
                stmt.setString(2, utente.getPassword());
                stmt.setString(3, utente.getNome());
                stmt.setString(4, utente.getCognome());
                stmt.setString(5, utente.getRuolo().name());
                stmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // LOGIN UTENTE
    public UtenteAutenticato login(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM utenti WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
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
            System.err.println("Errore nel login: " + e.getMessage());
        }
        return null;
    }

    // (OPZIONALE) LISTA TUTTI GLI UTENTI
    public List<UtenteAutenticato> getTuttiGliUtenti() {
        List<UtenteAutenticato> utenti = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM utenti")) {

            while (rs.next()) {
                UtenteAutenticato utente = new UtenteAutenticato(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        Ruolo.valueOf(rs.getString("ruolo"))
                );
                utenti.add(utente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utenti;
    }

    public boolean esisteUsername(String username) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT 1 FROM utenti WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean esistePersona(String nome, String cognome) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT 1 FROM utenti WHERE nome = ? AND cognome = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, cognome);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
