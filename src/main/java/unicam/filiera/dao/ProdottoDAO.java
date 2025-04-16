package unicam.filiera.dao;

import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

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

    /**
     * Salvataggio di un nuovo prodotto nel DB.
     * Aggiungiamo la colonna commento nel campo INSERT con un valore di default (null).
     */
    public boolean salvaProdotto(Prodotto prodotto, List<File> certificati, List<File> foto) {
        try (Connection conn = DatabaseManager.getConnection()) {

            // Copia fisica dei file (certificati e foto) e ottiene i nomi
            List<String> nomiCertificati = certificati.stream()
                    .map(file -> copiaFile(file, CERTIFICATI_DIR))
                    .collect(Collectors.toList());

            List<String> nomiFoto = foto.stream()
                    .map(file -> copiaFile(file, FOTO_DIR))
                    .collect(Collectors.toList());

            // Aggiungiamo il campo commento nell'INSERT, con valore di default null
            // (se nella tua tabella hai la colonna "commento" che può essere null)
            String sql = """
                INSERT INTO prodotti 
                    (nome, descrizione, quantita, prezzo, certificati, foto, creato_da, stato, commento)
                VALUES 
                    (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, prodotto.getNome());
                stmt.setString(2, prodotto.getDescrizione());
                stmt.setInt(3, prodotto.getQuantita());
                stmt.setDouble(4, prodotto.getPrezzo());
                stmt.setString(5, String.join(",", nomiCertificati));
                stmt.setString(6, String.join(",", nomiFoto));
                stmt.setString(7, prodotto.getCreatoDa());
                stmt.setString(8, "IN_ATTESA");

                // Se vuoi, puoi gestire un commento iniziale.
                // Se non esiste (quasi sempre), metti null.
                stmt.setNull(9, Types.VARCHAR);

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

    /**
     * Restituisce tutti i prodotti creati da un certo utente (creatoDa).
     * Include anche il commento, se presente in DB.
     */
    public List<Prodotto> getProdottiByCreatore(String creatoDa) {
        List<Prodotto> prodotti = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM prodotti WHERE creato_da = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, creatoDa);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<String> certificati = List.of(rs.getString("certificati").split(","));
                        List<String> foto = List.of(rs.getString("foto").split(","));
                        StatoProdotto stato = StatoProdotto.valueOf(rs.getString("stato"));

                        // Recupera il commento (potrebbe essere null)
                        String commento = rs.getString("commento");

                        // Usa il costruttore di Prodotto che gestisce il commento
                        Prodotto prodotto = new Prodotto(
                                rs.getString("nome"),
                                rs.getString("descrizione"),
                                rs.getInt("quantita"),
                                rs.getDouble("prezzo"),
                                certificati,
                                foto,
                                rs.getString("creato_da"),
                                stato,
                                commento
                        );

                        prodotti.add(prodotto);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prodotti;
    }

    /**
     * Restituisce tutti i prodotti nel DB, includendo il commento.
     */
    public List<Prodotto> getTuttiIProdotti() {
        List<Prodotto> prodotti = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM prodotti";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    List<String> certificati = List.of(rs.getString("certificati").split(","));
                    List<String> foto = List.of(rs.getString("foto").split(","));
                    StatoProdotto stato = StatoProdotto.valueOf(rs.getString("stato"));
                    String commento = rs.getString("commento"); // può essere null

                    Prodotto prodotto = new Prodotto(
                            rs.getString("nome"),
                            rs.getString("descrizione"),
                            rs.getInt("quantita"),
                            rs.getDouble("prezzo"),
                            certificati,
                            foto,
                            rs.getString("creato_da"),
                            stato,
                            commento
                    );
                    prodotti.add(prodotto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prodotti;
    }

    /**
     * Restituisce i prodotti filtrati per stato (ad es. IN_ATTESA, APPROVATO, RIFIUTATO),
     * includendo il commento (se presente).
     */
    public List<Prodotto> getProdottiByStato(StatoProdotto stato) {
        List<Prodotto> prodotti = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM prodotti WHERE stato = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, stato.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<String> certificati = List.of(rs.getString("certificati").split(","));
                        List<String> foto = List.of(rs.getString("foto").split(","));
                        String commento = rs.getString("commento");

                        Prodotto prodotto = new Prodotto(
                                rs.getString("nome"),
                                rs.getString("descrizione"),
                                rs.getInt("quantita"),
                                rs.getDouble("prezzo"),
                                certificati,
                                foto,
                                rs.getString("creato_da"),
                                StatoProdotto.valueOf(rs.getString("stato")),
                                commento
                        );
                        prodotti.add(prodotto);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prodotti;
    }

    /**
     * Aggiorna solo lo stato del prodotto (utile ad es. se non hai bisogno di un commento).
     * Se devi gestire il commento, usa il metodo aggiornaStatoECommentoProdotto.
     */
    public boolean aggiornaStatoProdotto(Prodotto prodotto, StatoProdotto nuovoStato) {
        String sql = "UPDATE prodotti SET stato = ? WHERE nome = ? AND creato_da = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setString(2, prodotto.getNome());
            stmt.setString(3, prodotto.getCreatoDa());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Errore aggiornamento stato prodotto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Nuovo metodo per aggiornare SIA lo stato SIA il commento di un prodotto.
     * Utile quando il curatore rifiuta un prodotto lasciando un commento.
     */
    public boolean aggiornaStatoECommentoProdotto(Prodotto prodotto,
                                                  StatoProdotto nuovoStato,
                                                  String nuovoCommento) {
        String sql = "UPDATE prodotti SET stato = ?, commento = ? WHERE nome = ? AND creato_da = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setString(2, nuovoCommento); // se è null, verrà salvato NULL nel DB
            stmt.setString(3, prodotto.getNome());
            stmt.setString(4, prodotto.getCreatoDa());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Errore aggiornamento stato e commento prodotto: " + e.getMessage());
            return false;
        }
    }
}
