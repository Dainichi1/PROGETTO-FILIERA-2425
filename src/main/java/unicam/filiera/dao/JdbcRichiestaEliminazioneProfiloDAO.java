package unicam.filiera.dao;

import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRichiestaEliminazioneProfiloDAO implements RichiestaEliminazioneProfiloDAO {

    private static JdbcRichiestaEliminazioneProfiloDAO instance;

    private static final String SQL_INSERT =
            "INSERT INTO richieste_eliminazione_profilo (username, stato, data_richiesta) " +
                    "VALUES (?, ?, CURRENT_TIMESTAMP)";

    private static final String SQL_UPDATE_STATO =
            "UPDATE richieste_eliminazione_profilo SET stato = ? WHERE id = ?";

    private static final String SQL_FIND_BY_STATO =
            "SELECT * FROM richieste_eliminazione_profilo WHERE stato = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM richieste_eliminazione_profilo WHERE id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM richieste_eliminazione_profilo";

    private static final String SQL_FIND_BY_USERNAME =
            "SELECT * FROM richieste_eliminazione_profilo WHERE username = ?";

    // richiesta pendente più recente per username
    private static final String SQL_FIND_PENDING_BY_USERNAME =
            "SELECT * FROM richieste_eliminazione_profilo " +
                    "WHERE username = ? AND stato = 'IN_ATTESA' " +
                    "ORDER BY data_richiesta DESC LIMIT 1";

    private JdbcRichiestaEliminazioneProfiloDAO() {}

    public static JdbcRichiestaEliminazioneProfiloDAO getInstance() {
        if (instance == null) instance = new JdbcRichiestaEliminazioneProfiloDAO();
        return instance;
    }

    @Override
    public boolean save(RichiestaEliminazioneProfilo richiesta) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, richiesta.getUsername());
            ps.setString(2, richiesta.getStato().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) richiesta.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(int richiestaId) {
        String sql = "DELETE FROM richieste_eliminazione_profilo WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, richiestaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean updateStato(int richiestaId, StatoRichiestaEliminazioneProfilo nuovoStato) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATO)) {

            ps.setString(1, nuovoStato.name());
            ps.setInt(2, richiestaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RichiestaEliminazioneProfilo> findByStato(StatoRichiestaEliminazioneProfilo stato) {
        List<RichiestaEliminazioneProfilo> out = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STATO)) {

            ps.setString(1, stato.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(buildFromRs(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    @Override
    public RichiestaEliminazioneProfilo findById(int richiestaId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, richiestaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildFromRs(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<RichiestaEliminazioneProfilo> findAll() {
        List<RichiestaEliminazioneProfilo> out = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) out.add(buildFromRs(rs));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    @Override
    public List<RichiestaEliminazioneProfilo> findByUsername(String username) {
        List<RichiestaEliminazioneProfilo> out = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(buildFromRs(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    /** Restituisce la richiesta in attesa più recente per username, se presente. */
    public Optional<RichiestaEliminazioneProfilo> findPendingByUsername(String username) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_PENDING_BY_USERNAME)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(buildFromRs(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    // ---- helper ----
    private RichiestaEliminazioneProfilo buildFromRs(ResultSet rs) throws SQLException {
        return new RichiestaEliminazioneProfilo(
                rs.getInt("id"),
                rs.getString("username"),
                StatoRichiestaEliminazioneProfilo.valueOf(rs.getString("stato")),
                rs.getTimestamp("data_richiesta").toLocalDateTime()
        );
    }
}
