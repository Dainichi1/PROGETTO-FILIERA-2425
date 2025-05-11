package unicam.filiera.dao;

import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione JDBC di FieraDAO.
 */
public class JdbcFieraDAO implements FieraDAO {
    private static JdbcFieraDAO instance;

    private JdbcFieraDAO() {
        // eventuali inizializzazioni
    }

    public static JdbcFieraDAO getInstance() {
        if (instance == null) {
            instance = new JdbcFieraDAO();
        }
        return instance;
    }

    @Override
    public boolean save(Fiera fiera) {
        String sql = """
                INSERT INTO fiere
                  (data_inizio, data_fine, prezzo, descrizione, indirizzo, organizzatore, numero_min_partecipanti, stato)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(fiera.getDataInizio()));
            ps.setTimestamp(2, Timestamp.valueOf(fiera.getDataFine()));
            ps.setDouble(3, fiera.getPrezzo());
            ps.setString(4, fiera.getDescrizione());
            ps.setString(5, fiera.getIndirizzo());
            ps.setString(6, fiera.getOrganizzatore());
            ps.setInt(7, fiera.getNumeroMinPartecipanti());
            ps.setString(8, fiera.getStato().name());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        fiera.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Fiera fiera) {
        String sql = """
                UPDATE fiere
                   SET data_inizio = ?,
                       data_fine = ?,
                       prezzo = ?,
                       descrizione = ?,
                       indirizzo = ?,
                       numero_min_partecipanti = ?,
                       stato = ?
                 WHERE id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fiera.getDataInizio()));
            ps.setTimestamp(2, Timestamp.valueOf(fiera.getDataFine()));
            ps.setDouble(3, fiera.getPrezzo());
            ps.setString(4, fiera.getDescrizione());
            ps.setString(5, fiera.getIndirizzo());
            ps.setInt(6, fiera.getNumeroMinPartecipanti());
            ps.setString(7, fiera.getStato().name());
            ps.setLong(8, fiera.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Fiera> findByOrganizzatore(String organizzatore) {
        String sql = """
                SELECT * 
                  FROM fiere 
                 WHERE organizzatore = ?
                """;
        return findBy(sql, organizzatore);
    }

    @Override
    public List<Fiera> findByStato(StatoEvento stato) {
        // usa il tuo helper findBy(sql,param)
        return findBy(
                "SELECT * FROM fiere WHERE stato = ?",
                stato.name()
        );
    }


    @Override
    public List<Fiera> findAll() {
        String sql = """
                SELECT * 
                  FROM fiere
                """;
        return findBy(sql, null);
    }

    @Override
    public Fiera findById(long id) {
        String sql = """
                SELECT * 
                  FROM fiere 
                 WHERE id = ?
                """;
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

    // --- helper generico per findBy... ---
    private List<Fiera> findBy(String sql, String param) {
        List<Fiera> list = new ArrayList<>();
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

    // Costruisce l’oggetto Fiera dal ResultSet
    private Fiera buildFromRs(ResultSet rs) throws SQLException {
        return new Fiera.Builder()
                .id(rs.getLong("id"))
                .dataInizio(rs.getTimestamp("data_inizio").toLocalDateTime())
                .dataFine(rs.getTimestamp("data_fine").toLocalDateTime())
                .prezzo(rs.getDouble("prezzo"))
                .descrizione(rs.getString("descrizione"))
                .indirizzo(rs.getString("indirizzo"))
                .organizzatore(rs.getString("organizzatore"))
                .numeroMinPartecipanti(rs.getInt("numero_min_partecipanti"))
                .stato(StatoEvento.valueOf(rs.getString("stato")))
                .build();
    }
}
