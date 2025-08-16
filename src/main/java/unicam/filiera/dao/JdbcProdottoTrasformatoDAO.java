package unicam.filiera.dao;

import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.FaseProduzione;

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

public class JdbcProdottoTrasformatoDAO implements ProdottoTrasformatoDAO {
    private static JdbcProdottoTrasformatoDAO instance;
    private static final String CERT_DIR = "uploads/certificati";
    private static final String FOTO_DIR = "uploads/foto";

    private JdbcProdottoTrasformatoDAO() {
        new File(CERT_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    public static JdbcProdottoTrasformatoDAO getInstance() {
        if (instance == null) instance = new JdbcProdottoTrasformatoDAO();
        return instance;
    }

    @Override
    public boolean save(ProdottoTrasformato p, List<File> certFiles, List<File> fotoFiles) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            String insert = """
                        INSERT INTO prodotti_trasformati
                          (nome, descrizione, quantita, prezzo, indirizzo, certificati, foto, creato_da, stato, commento)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            int prodottoId = -1;
            try (PreparedStatement st = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, p.getNome());
                st.setString(2, p.getDescrizione());
                st.setInt(3, p.getQuantita());
                st.setDouble(4, p.getPrezzo());
                st.setString(5, p.getIndirizzo());
                st.setString(6, "");
                st.setString(7, "");
                st.setString(8, p.getCreatoDa());
                st.setString(9, p.getStato().name());
                st.setString(10, p.getCommento());
                st.executeUpdate();

                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        prodottoId = rs.getInt(1);
                    }
                }
            }

            // Inserisci fasi produzione
            for (FaseProduzione fase : p.getFasiProduzione()) {
                String insertFase = """
                            INSERT INTO fasi_produzione
                                (prodotto_trasformato_id, descrizione, produttore, prodotto_origine)
                            VALUES (?, ?, ?, ?)
                        """;
                try (PreparedStatement stFase = conn.prepareStatement(insertFase)) {
                    stFase.setInt(1, prodottoId);
                    stFase.setString(2, fase.getDescrizioneFase());
                    stFase.setString(3, fase.getProduttoreUsername());
                    stFase.setString(4, fase.getProdottoOrigine());
                    stFase.executeUpdate();
                }
            }

            // Upload file e update record
            List<String> certNames = certFiles.stream()
                    .map(f -> copiaFile(f, CERT_DIR))
                    .collect(Collectors.toList());
            List<String> fotoNames = fotoFiles.stream()
                    .map(f -> copiaFile(f, FOTO_DIR))
                    .collect(Collectors.toList());

            String upd = "UPDATE prodotti_trasformati SET certificati = ?, foto = ? WHERE id = ?";
            try (PreparedStatement st = conn.prepareStatement(upd)) {
                st.setString(1, String.join(",", certNames));
                st.setString(2, String.join(",", fotoNames));
                st.setInt(3, prodottoId);
                st.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                DatabaseManager.getConnection().rollback();
            } catch (Exception ignore) {
            }
            return false;
        }
    }

    @Override
    public boolean update(String nomeOriginale, String creatore, ProdottoTrasformato p, List<File> certFiles, List<File> fotoFiles) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            // Recupero id prodotto_trasformato da modificare
            int prodottoId = -1;
            String sqlId = "SELECT id FROM prodotti_trasformati WHERE nome = ? AND creato_da = ?";
            try (PreparedStatement stId = conn.prepareStatement(sqlId)) {
                stId.setString(1, nomeOriginale);
                stId.setString(2, creatore);
                try (ResultSet rs = stId.executeQuery()) {
                    if (rs.next()) {
                        prodottoId = rs.getInt("id");
                    }
                }
            }
            if (prodottoId == -1) throw new SQLException("Prodotto trasformato non trovato");

            // Upload file
            List<String> certNames = certFiles.stream()
                    .map(f -> copiaFile(f, CERT_DIR))
                    .collect(Collectors.toList());
            List<String> fotoNames = fotoFiles.stream()
                    .map(f -> copiaFile(f, FOTO_DIR))
                    .collect(Collectors.toList());

            // Aggiorna tutti i campi principali
            String sqlUpd = """
                        UPDATE prodotti_trasformati
                           SET nome = ?, descrizione = ?, quantita = ?, prezzo = ?, indirizzo = ?,
                               certificati = ?, foto = ?, stato = ?, commento = ?
                         WHERE id = ?
                    """;
            try (PreparedStatement st = conn.prepareStatement(sqlUpd)) {
                st.setString(1, p.getNome());
                st.setString(2, p.getDescrizione());
                st.setInt(3, p.getQuantita());
                st.setDouble(4, p.getPrezzo());
                st.setString(5, p.getIndirizzo());
                st.setString(6, String.join(",", certNames));
                st.setString(7, String.join(",", fotoNames));
                st.setString(8, p.getStato().name());
                st.setString(9, p.getCommento());
                st.setInt(10, prodottoId);
                st.executeUpdate();
            }

            // Cancella tutte le fasi precedenti
            try (PreparedStatement stDelFasi = conn.prepareStatement("DELETE FROM fasi_produzione WHERE prodotto_trasformato_id = ?")) {
                stDelFasi.setInt(1, prodottoId);
                stDelFasi.executeUpdate();
            }
            // Reinserisce le fasi aggiornate
            for (FaseProduzione fase : p.getFasiProduzione()) {
                String insertFase = """
                            INSERT INTO fasi_produzione
                                (prodotto_trasformato_id, descrizione, produttore, prodotto_origine)
                            VALUES (?, ?, ?, ?)
                        """;
                try (PreparedStatement stFase = conn.prepareStatement(insertFase)) {
                    stFase.setInt(1, prodottoId);
                    stFase.setString(2, fase.getDescrizioneFase());
                    stFase.setString(3, fase.getProduttoreUsername());
                    stFase.setString(4, fase.getProdottoOrigine());
                    stFase.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                DatabaseManager.getConnection().rollback();
            } catch (Exception ignore) {
            }
            return false;
        }
    }

    @Override
    public boolean update(ProdottoTrasformato p) {
        // Aggiorna solo campi descrizione, quantita, prezzo, indirizzo, stato, commento (NO FILE e NO FASI)
        String sql = """
                UPDATE prodotti_trasformati
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

    @Override
    public boolean deleteByNomeAndCreatore(String nome, String creatore) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Recupera id per cancellare anche fasi_produzione
            int prodottoId = -1;
            String sqlId = "SELECT id FROM prodotti_trasformati WHERE nome = ? AND creato_da = ?";
            try (PreparedStatement stId = conn.prepareStatement(sqlId)) {
                stId.setString(1, nome);
                stId.setString(2, creatore);
                try (ResultSet rs = stId.executeQuery()) {
                    if (rs.next()) prodottoId = rs.getInt("id");
                }
            }
            if (prodottoId == -1) return false;

            // Cancella fasi_produzione collegate
            try (PreparedStatement stFasi = conn.prepareStatement("DELETE FROM fasi_produzione WHERE prodotto_trasformato_id = ?")) {
                stFasi.setInt(1, prodottoId);
                stFasi.executeUpdate();
            }
            // Cancella prodotto
            String sql = "DELETE FROM prodotti_trasformati WHERE id = ?";
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, prodottoId);
                return st.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ProdottoTrasformato> findByCreatore(String creatore) {
        List<ProdottoTrasformato> list = new ArrayList<>();
        String sql = "SELECT * FROM prodotti_trasformati WHERE creato_da = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, creatore);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(buildProdottoTrasformatoFromRs(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ProdottoTrasformato> findByStato(StatoProdotto stato) {
        List<ProdottoTrasformato> list = new ArrayList<>();
        String sql = "SELECT * FROM prodotti_trasformati WHERE stato = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, stato.name());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(buildProdottoTrasformatoFromRs(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ProdottoTrasformato> findAll() {
        List<ProdottoTrasformato> list = new ArrayList<>();
        String sql = "SELECT * FROM prodotti_trasformati";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(buildProdottoTrasformatoFromRs(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ProdottoTrasformato findByNome(String nome) {
        String sql = "SELECT * FROM prodotti_trasformati WHERE nome = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, nome);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return buildProdottoTrasformatoFromRs(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ProdottoTrasformato findByNomeAndCreatore(String nome, String creatore) {
        String sql = "SELECT * FROM prodotti_trasformati WHERE nome = ? AND creato_da = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, nome);
            st.setString(2, creatore);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return buildProdottoTrasformatoFromRs(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper per copiare file
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

    // Helper per ricostruire l'oggetto ProdottoTrasformato da ResultSet
    private ProdottoTrasformato buildProdottoTrasformatoFromRs(ResultSet rs) throws SQLException {
        String certCsv = rs.getString("certificati");
        List<String> cert = (certCsv == null || certCsv.isBlank())
                ? List.of()
                : Arrays.asList(certCsv.split(","));

        String fotoCsv = rs.getString("foto");
        List<String> foto = (fotoCsv == null || fotoCsv.isBlank())
                ? List.of()
                : Arrays.asList(fotoCsv.split(","));

        int id = rs.getInt("id");

        // Recupera fasi_produzione associate
        List<FaseProduzione> fasi = new ArrayList<>();
        String sqlFasi = "SELECT * FROM fasi_produzione WHERE prodotto_trasformato_id = ?";
        try (Connection conn2 = DatabaseManager.getConnection();
             PreparedStatement st = conn2.prepareStatement(sqlFasi)) {
            st.setInt(1, id);
            try (ResultSet rsFasi = st.executeQuery()) {
                while (rsFasi.next()) {
                    FaseProduzione fase = new FaseProduzione(
                            rsFasi.getString("descrizione"),
                            rsFasi.getString("produttore"), // qui puoi convertire in oggetto se vuoi!
                            rsFasi.getString("prodotto_origine")
                    );
                    fasi.add(fase);
                }
            }
        }

        return new ProdottoTrasformato.Builder()
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
                .fasiProduzione(fasi)
                .build();
    }
}
