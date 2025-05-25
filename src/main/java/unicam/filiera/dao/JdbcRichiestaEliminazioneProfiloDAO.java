package unicam.filiera.dao;

import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcRichiestaEliminazioneProfiloDAO implements RichiestaEliminazioneProfiloDAO {

    private static JdbcRichiestaEliminazioneProfiloDAO instance;

    private JdbcRichiestaEliminazioneProfiloDAO() {}

    public static JdbcRichiestaEliminazioneProfiloDAO getInstance() {
        if (instance == null) instance = new JdbcRichiestaEliminazioneProfiloDAO();
        return instance;
    }

    @Override
    public boolean save(RichiestaEliminazioneProfilo richiesta) {
        String sql = "INSERT INTO richieste_eliminazione_profilo (username, stato, data_richiesta) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, richiesta.getUsername());
            ps.setString(2, richiesta.getStato().name());
            ps.setTimestamp(3, Timestamp.valueOf(richiesta.getDataRichiesta()));
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) richiesta.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateStato(int richiestaId, StatoRichiestaEliminazioneProfilo nuovoStato) {
        String sql = "UPDATE richieste_eliminazione_profilo SET stato = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuovoStato.name());
            ps.setInt(2, richiestaId);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RichiestaEliminazioneProfilo> findByStato(StatoRichiestaEliminazioneProfilo stato) {
        List<RichiestaEliminazioneProfilo> richieste = new ArrayList<>();
        String sql = "SELECT * FROM richieste_eliminazione_profilo WHERE stato = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stato.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) richieste.add(buildFromRs(rs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return richieste;
    }

    @Override
    public RichiestaEliminazioneProfilo findById(int richiestaId) {
        String sql = "SELECT * FROM richieste_eliminazione_profilo WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, richiestaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildFromRs(rs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<RichiestaEliminazioneProfilo> findAll() {
        List<RichiestaEliminazioneProfilo> richieste = new ArrayList<>();
        String sql = "SELECT * FROM richieste_eliminazione_profilo";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) richieste.add(buildFromRs(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return richieste;
    }

    @Override
    public List<RichiestaEliminazioneProfilo> findByUsername(String username){
        List<RichiestaEliminazioneProfilo> richieste = new ArrayList<>();
        String sql = "SELECT * FROM richieste_eliminazione_profilo WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) richieste.add(buildFromRs(rs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return richieste;
    }

    // Helper privato per build oggetto da ResultSet
    private RichiestaEliminazioneProfilo buildFromRs(ResultSet rs) throws SQLException {
        return new RichiestaEliminazioneProfilo(
                rs.getInt("id"),
                rs.getString("username"),
                StatoRichiestaEliminazioneProfilo.valueOf(rs.getString("stato")),
                rs.getTimestamp("data_richiesta").toLocalDateTime()
        );
    }
}
