package unicam.filiera.dao;

import unicam.filiera.model.Prodotto;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProdottoDAO {

    private static final String CERTIFICATI_DIR = "uploads/certificati";
    private static final String FOTO_DIR = "uploads/foto";

    public ProdottoDAO() {
        creaCartelleSeNonEsistono();
    }

    private void creaCartelleSeNonEsistono() {
        new File(CERTIFICATI_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    public boolean salvaProdotto(Prodotto prodotto, List<File> certificati, List<File> foto) {
        try (Connection conn = DatabaseManager.getConnection()) {

            // Copia file e ottieni nomi
            List<String> nomiCertificati = certificati.stream()
                    .map(file -> copiaFile(file, CERTIFICATI_DIR))
                    .collect(Collectors.toList());

            List<String> nomiFoto = foto.stream()
                    .map(file -> copiaFile(file, FOTO_DIR))
                    .collect(Collectors.toList());

            String sql = """
                INSERT INTO prodotti (nome, descrizione, quantita, prezzo, certificati, foto, creato_da)
                VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, prodotto.getNome());
                stmt.setString(2, prodotto.getDescrizione());
                stmt.setInt(3, prodotto.getQuantita());
                stmt.setDouble(4, prodotto.getPrezzo());
                stmt.setString(5, String.join(",", nomiCertificati));
                stmt.setString(6, String.join(",", nomiFoto));
                stmt.setString(7, prodotto.getCreatoDa());

                stmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore salvataggio prodotto: " + e.getMessage());
            return false;
        }
    }

    private String copiaFile(File file, String destinazione) {
        try {
            Path destPath = Paths.get(destinazione, file.getName());
            Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            return file.getName();
        } catch (IOException e) {
            e.printStackTrace();
            return "errore_" + file.getName();
        }
    }


    public List<Prodotto> getProdottiByCreatore(String creatoDa) {
        List<Prodotto> prodotti = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM prodotti WHERE creato_da = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, creatoDa);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    List<String> certificati = List.of(rs.getString("certificati").split(","));
                    List<String> foto = List.of(rs.getString("foto").split(","));

                    Prodotto prodotto = new Prodotto(
                            rs.getString("nome"),
                            rs.getString("descrizione"),
                            rs.getInt("quantita"),
                            rs.getDouble("prezzo"),
                            certificati,
                            foto,
                            rs.getString("creato_da")
                    );

                    prodotti.add(prodotto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prodotti;
    }

    public List<Prodotto> getTuttiIProdotti() {
        List<Prodotto> prodotti = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM prodotti";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    List<String> certificati = List.of(rs.getString("certificati").split(","));
                    List<String> foto = List.of(rs.getString("foto").split(","));
                    Prodotto prodotto = new Prodotto(
                            rs.getString("nome"),
                            rs.getString("descrizione"),
                            rs.getInt("quantita"),
                            rs.getDouble("prezzo"),
                            certificati,
                            foto,
                            rs.getString("creato_da")
                    );
                    prodotti.add(prodotto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prodotti;
    }




}
