package unicam.filiera.dao;

import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione JDBC di PacchettoDAO.
 */
public class JdbcPacchettoDAO implements PacchettoDAO {
    private static JdbcPacchettoDAO instance;
    private static final String CERT_DIR = "uploads/certificati_pacchetti";
    private static final String FOTO_DIR = "uploads/foto_pacchetti";

    private JdbcPacchettoDAO() {
        new File(CERT_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    public static JdbcPacchettoDAO getInstance() {
        if (instance == null) {
            instance = new JdbcPacchettoDAO();
        }
        return instance;
    }

    @Override
    public boolean save(Pacchetto p, List<File> certFiles, List<File> fotoFiles) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1) inserisci i dati testuali
            String insert = """
                        INSERT INTO pacchetti
                          (nome, descrizione, indirizzo, prezzo_totale, quantita, prodotti, certificati, foto, creato_da, stato, commento)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement st = conn.prepareStatement(insert)) {
                st.setString(1, p.getNome());
                st.setString(2, p.getDescrizione());
                st.setString(3, p.getIndirizzo());
                st.setDouble(4, p.getPrezzoTotale());
                st.setInt(5, p.getQuantita());
                st.setString(6,
                        p.getProdotti().stream()
                                .map(Prodotto::getNome)
                                .collect(Collectors.joining(","))
                );
                // placeholder per i file
                st.setString(7, "");
                st.setString(8, "");
                st.setString(9, p.getCreatoDa());
                st.setString(10, p.getStato().name());
                st.setNull(11, Types.VARCHAR);
                st.executeUpdate();
            }

            // 2) upload file e aggiornamento record
            List<String> certNames = certFiles.stream()
                    .map(f -> copiaFile(f, CERT_DIR))
                    .collect(Collectors.toList());
            List<String> fotoNames = fotoFiles.stream()
                    .map(f -> copiaFile(f, FOTO_DIR))
                    .collect(Collectors.toList());

            String update = "UPDATE pacchetti SET certificati = ?, foto = ? WHERE nome = ? AND creato_da = ?";
            try (PreparedStatement st = conn.prepareStatement(update)) {
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

    public boolean update(Pacchetto p) {
        String sql = """
                    UPDATE pacchetti
                       SET nome         = ?,
                           descrizione  = ?,
                           indirizzo    = ?,
                           prezzo_totale= ?,
                           quantita     = ?,            
                           prodotti     = ?,
                           certificati  = ?,
                           foto         = ?,
                           stato        = ?,
                           commento     = ?
                     WHERE nome = ? AND creato_da = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getDescrizione());
            stmt.setString(3, p.getIndirizzo());
            stmt.setDouble(4, p.getPrezzoTotale());
            stmt.setInt(5, p.getQuantita());
            stmt.setString(6, p.getProdotti().stream()
                    .map(Prodotto::getNome).collect(Collectors.joining(",")));
            stmt.setString(7, String.join(",", p.getCertificati()));
            stmt.setString(8, String.join(",", p.getFoto()));
            stmt.setString(9, p.getStato().name());
            stmt.setString(10, p.getCommento());
            stmt.setString(11, p.getNome());
            stmt.setString(12, p.getCreatoDa());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Full-update: modifica tutti i campi del pacchetto (anche nome),
     * più re-upload di certificati e foto.
     */
    @Override
    public boolean update(
            String nomeOriginale,
            String creatore,
            Pacchetto p,
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

            // 2) full UPDATE di tutti i campi
            String sql = """
                        UPDATE pacchetti
                           SET nome         = ?,
                               descrizione  = ?,
                               indirizzo    = ?,
                               prezzo_totale= ?,
                               quantita     = ?,
                               prodotti     = ?,
                               certificati  = ?,
                               foto         = ?,
                               stato        = ?,
                               commento     = ?
                         WHERE nome = ? AND creato_da = ?
                    """;
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, p.getNome());
                st.setString(2, p.getDescrizione());
                st.setString(3, p.getIndirizzo());
                st.setDouble(4, p.getPrezzoTotale());
                st.setInt(5, p.getQuantita());
                st.setString(6, p.getProdotti().stream()
                        .map(Prodotto::getNome)
                        .collect(Collectors.joining(",")));
                st.setString(7, String.join(",", certNames));
                st.setString(8, String.join(",", fotoNames));
                st.setString(9, p.getStato().name());
                st.setString(10, p.getCommento());
                st.setString(11, nomeOriginale);
                st.setString(12, creatore);

                int updated = st.executeUpdate();
                conn.commit();
                return updated > 0;
            }
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
    public boolean aggiornaQuantita(String nome, int nuovaQuantita) {
        String sql = "UPDATE pacchetti SET quantita = ? WHERE nome = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, nuovaQuantita);
            st.setString(2, nome);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public List<Pacchetto> findByCreatore(String creatore) {
        return findBy("SELECT * FROM pacchetti WHERE creato_da = ?", creatore);
    }

    @Override
    public List<Pacchetto> findByStato(StatoProdotto stato) {
        return findBy("SELECT * FROM pacchetti WHERE stato = ?", stato.name());
    }

    @Override
    public List<Pacchetto> findAll() {
        return findBy("SELECT * FROM pacchetti", null);
    }

    @Override
    public Pacchetto findByNomeAndCreatore(String nome, String creatore) {
        String sql = "SELECT * FROM pacchetti WHERE nome = ? AND creato_da = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, creatore);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return buildFromRs(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean deleteByNomeAndCreatore(String nome, String creatore) {
        String sql = "DELETE FROM pacchetti WHERE nome = ? AND creato_da = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, creatore);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper generico per query di lettura
    private List<Pacchetto> findBy(String sql, String param) {
        List<Pacchetto> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param != null) stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(buildFromRs(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Costruisce un Pacchetto da ResultSet
    private Pacchetto buildFromRs(ResultSet rs) throws SQLException {
        String certCsv = rs.getString("certificati");
        List<String> cert = (certCsv == null || certCsv.isBlank())
                ? List.of()
                : Arrays.asList(certCsv.split(","));

        String fotoCsv = rs.getString("foto");
        List<String> foto = (fotoCsv == null || fotoCsv.isBlank())
                ? List.of()
                : Arrays.asList(fotoCsv.split(","));

        return new Pacchetto.Builder()
                .nome(rs.getString("nome"))
                .descrizione(rs.getString("descrizione"))
                .indirizzo(rs.getString("indirizzo"))
                .prezzoTotale(rs.getDouble("prezzo_totale"))
                .quantita(rs.getInt("quantita"))
                .prodotti(recuperaProdotti(rs.getString("prodotti")))
                .certificati(cert)
                .foto(foto)
                .creatoDa(rs.getString("creato_da"))
                .stato(StatoProdotto.valueOf(rs.getString("stato")))
                .commento(rs.getString("commento"))
                .build();
    }

    // Recupera la lista di Prodotto a partire dalla stringa "nome1,nome2,…"
    private List<Prodotto> recuperaProdotti(String prodottiString) {
        List<Prodotto> prodotti = new ArrayList<>();
        if (prodottiString == null || prodottiString.isBlank()) return prodotti;

        ProdottoDAO dao = JdbcProdottoDAO.getInstance();
        String[] nomi = prodottiString.split(",");

        for (String nome : nomi) {
            Prodotto p = dao.findByNome(nome.trim());
            if (p != null) {
                prodotti.add(p);
            } else {
                System.err.println("Prodotto non trovato: " + nome.trim());
            }
        }
        return prodotti;
    }

    private String copiaFile(File file, String destDir) {
        try {
            Path dest = Paths.get(destDir, file.getName());
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return file.getName();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}