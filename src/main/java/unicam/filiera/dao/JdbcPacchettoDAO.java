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

    /** 1) salva solo i campi testuali, senza certificati/foto */
    @Override
    public boolean saveDetails(Pacchetto p) {
        String insertSql = """
            INSERT INTO pacchetti
              (nome, descrizione, indirizzo, prezzo_totale, prodotti,
               certificati, foto, creato_da, stato, commento)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getDescrizione());
            stmt.setString(3, p.getIndirizzo());
            stmt.setDouble(4, p.getPrezzoTotale());
            stmt.setString(5,
                    p.getProdotti().stream()
                            .map(Prodotto::getNome)
                            .collect(Collectors.joining(",")));
            stmt.setString(6, "");  // placeholder
            stmt.setString(7, "");
            stmt.setString(8, p.getCreatoDa());
            stmt.setString(9, p.getStato().name());
            stmt.setNull(10, Types.VARCHAR);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** 2) copia fisicamente i file e aggiorna le colonne */
    @Override
    public boolean saveFiles(Pacchetto p, List<File> certFiles, List<File> fotoFiles) {
        String updateFiles =
                "UPDATE pacchetti SET certificati = ?, foto = ? WHERE nome = ? AND creato_da = ?";
        List<String> certNames = certFiles.stream()
                .map(f -> copiaFile(f, CERT_DIR))
                .collect(Collectors.toList());
        List<String> fotoNames = fotoFiles.stream()
                .map(f -> copiaFile(f, FOTO_DIR))
                .collect(Collectors.toList());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateFiles)) {

            stmt.setString(1, String.join(",", certNames));
            stmt.setString(2, String.join(",", fotoNames));
            stmt.setString(3, p.getNome());
            stmt.setString(4, p.getCreatoDa());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Pacchetto p) {
        String sql = """
            UPDATE pacchetti
               SET descrizione   = ?,
                   prezzo_totale = ?,
                   indirizzo     = ?,
                   stato         = ?,
                   commento      = ?
             WHERE nome = ? AND creato_da = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getDescrizione());
            stmt.setDouble(2, p.getPrezzoTotale());
            stmt.setString(3, p.getIndirizzo());
            stmt.setString(4, p.getStato().name());
            stmt.setString(5, p.getCommento());
            stmt.setString(6, p.getNome());
            stmt.setString(7, p.getCreatoDa());
            return stmt.executeUpdate() > 0;
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

    // --- helper generico per le query di lettura ---
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

    // Crea un Pacchetto da ResultSet
    private Pacchetto buildFromRs(ResultSet rs) throws SQLException {
        List<String> cert = rs.getString("certificati")==null
                ? List.of()
                : Arrays.asList(rs.getString("certificati").split(","));
        List<String> foto = rs.getString("foto")==null
                ? List.of()
                : Arrays.asList(rs.getString("foto").split(","));

        return new Pacchetto.Builder()
                .nome(rs.getString("nome"))
                .descrizione(rs.getString("descrizione"))
                .indirizzo(rs.getString("indirizzo"))
                .prezzoTotale(rs.getDouble("prezzo_totale"))
                .prodotti(recuperaProdotti(rs.getString("prodotti")))
                .certificati(cert)
                .foto(foto)
                .creatoDa(rs.getString("creato_da"))
                .stato(StatoProdotto.valueOf(rs.getString("stato")))
                .commento(rs.getString("commento"))
                .build();
    }

    // Estrae la lista di Prodotto a partire dalla stringa "nome1,nome2,…"

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