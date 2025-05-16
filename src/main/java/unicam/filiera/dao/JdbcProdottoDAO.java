package unicam.filiera.dao;

import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione JDBC di ProdottoDAO.
 */
public class JdbcProdottoDAO implements ProdottoDAO {
    private static JdbcProdottoDAO instance;
    private static final String CERT_DIR = "uploads/certificati";
    private static final String FOTO_DIR = "uploads/foto";

    private JdbcProdottoDAO() {
        new File(CERT_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    public static JdbcProdottoDAO getInstance() {
        if (instance == null) instance = new JdbcProdottoDAO();
        return instance;
    }

    @Override
    public boolean save(Prodotto p, List<File> certFiles, List<File> fotoFiles) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1) inserimento dettagli iniziali
            String insert = """
                    INSERT INTO prodotti
                      (nome, descrizione, quantita, prezzo, indirizzo, certificati, foto, creato_da, stato, commento)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement st = conn.prepareStatement(insert)) {
                st.setString(1, p.getNome());
                st.setString(2, p.getDescrizione());
                st.setInt(3, p.getQuantita());
                st.setDouble(4, p.getPrezzo());
                st.setString(5, p.getIndirizzo());
                st.setString(6, "");
                st.setString(7, "");
                st.setString(8, p.getCreatoDa());
                st.setString(9, p.getStato().name());
                st.setNull(10, Types.VARCHAR);
                st.executeUpdate();
            }

            // 2) upload file e aggiornamento record
            List<String> certNames = certFiles.stream()
                    .map(f -> copiaFile(f, CERT_DIR))
                    .collect(Collectors.toList());
            List<String> fotoNames = fotoFiles.stream()
                    .map(f -> copiaFile(f, FOTO_DIR))
                    .collect(Collectors.toList());

            String upd = "UPDATE prodotti SET certificati = ?, foto = ? WHERE nome = ? AND creato_da = ?";
            try (PreparedStatement st = conn.prepareStatement(upd)) {
                st.setString(1, String.join(",", certNames));
                st.setString(2, String.join(",", fotoNames));
                st.setString(3, p.getNome());
                st.setString(4, p.getCreatoDa());
                st.executeUpdate();
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Prodotto p) {
        // update solo dei campi descrizione/quantita/prezzo/indirizzo/stato/commento
        String sql = """
                UPDATE prodotti
                   SET descrizione = ?, quantita = ?, prezzo = ?, indirizzo = ?, stato = ?, commento = ?
                 WHERE nome = ? AND creato_da = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, p.getDescrizione());
            st.setInt(2, p.getQuantita());
            st.setDouble(3, p.getPrezzo());
            st.setString(4, p.getIndirizzo());
            st.setString(5, p.getStato().name());
            st.setString(6, p.getCommento());
            st.setString(7, p.getNome());
            st.setString(8, p.getCreatoDa());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Full‐update: modifica TUTTI i campi del prodotto (anche nome),
     * più re‐upload di certificati e foto.
     */
    @Override
    public boolean update(
            String nomeOriginale,
            String creatore,
            Prodotto p,
            List<File> certFiles,
            List<File> fotoFiles
    ) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            // 1) upload nuovi file
            List<String> certNames = certFiles.stream()
                    .map(f -> copiaFile(f, CERT_DIR))
                    .collect(Collectors.toList());
            List<String> fotoNames = fotoFiles.stream()
                    .map(f -> copiaFile(f, FOTO_DIR))
                    .collect(Collectors.toList());

            // 2) update di tutti i campi, incluse le liste file
            String sql = """
                    UPDATE prodotti
                       SET nome = ?, descrizione = ?, quantita = ?, prezzo = ?, indirizzo = ?,
                           certificati = ?, foto = ?, stato = ?, commento = ?
                     WHERE nome = ? AND creato_da = ?
                    """;
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, p.getNome());
                st.setString(2, p.getDescrizione());
                st.setInt(3, p.getQuantita());
                st.setDouble(4, p.getPrezzo());
                st.setString(5, p.getIndirizzo());
                st.setString(6, String.join(",", certNames));
                st.setString(7, String.join(",", fotoNames));
                st.setString(8, p.getStato().name());
                st.setString(9, p.getCommento());
                // WHERE
                st.setString(10, nomeOriginale);
                st.setString(11, creatore);
                int updated = st.executeUpdate();
                conn.commit();
                return updated > 0;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            // se qualcosa va storto, proviamo a rollback
            try {
                DatabaseManager.getConnection().rollback();
            } catch (Exception ignore) {
            }
            return false;
        }
    }

    @Override
    public Prodotto findByNomeAndCreatore(String nome, String creatore) {
        String sql = """
                    SELECT * FROM prodotti
                     WHERE nome = ? 
                       AND creato_da = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, nome);
            st.setString(2, creatore);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return buildProdottoFromRs(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean deleteByNomeAndCreatore(String nome, String creatore) {
        String sql = "DELETE FROM prodotti WHERE nome = ? AND creato_da = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, creatore);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Prodotto> findByCreatore(String creatore) {
        List<Prodotto> list = new ArrayList<>();
        String sql = "SELECT * FROM prodotti WHERE creato_da = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, creatore);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(buildProdottoFromRs(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Prodotto> findByStato(StatoProdotto stato) {
        List<Prodotto> list = new ArrayList<>();
        String sql = "SELECT * FROM prodotti WHERE stato = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, stato.name());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(buildProdottoFromRs(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Prodotto> findAll() {
        List<Prodotto> list = new ArrayList<>();
        String sql = "SELECT * FROM prodotti";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(buildProdottoFromRs(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Prodotto findByNome(String nome) {
        String sql = "SELECT * FROM prodotti WHERE nome = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, nome);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return buildProdottoFromRs(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- helper privati ----------

    private String copiaFile(File f, String destDir) {
        try {
            Path dst = Paths.get(destDir, f.getName());
            Files.copy(f.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
            return f.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private Prodotto buildProdottoFromRs(ResultSet rs) throws SQLException {
        String certCsv = rs.getString("certificati");
        List<String> cert = (certCsv == null || certCsv.isBlank())
                ? List.of()
                : Arrays.asList(certCsv.split(","));

        String fotoCsv = rs.getString("foto");
        List<String> foto = (fotoCsv == null || fotoCsv.isBlank())
                ? List.of()
                : Arrays.asList(fotoCsv.split(","));

        return new Prodotto.Builder()
                .nome(rs.getString("nome"))
                .descrizione(rs.getString("descrizione"))
                .quantita(rs.getInt("quantita"))
                .prezzo(rs.getDouble("prezzo"))
                .indirizzo(rs.getString("indirizzo"))
                .certificati(cert)
                .foto(foto)
                .creatoDa(rs.getString("creato_da"))
                .stato(StatoProdotto.valueOf(rs.getString("stato")))
                .commento(rs.getString("commento"))
                .build();
    }
}
