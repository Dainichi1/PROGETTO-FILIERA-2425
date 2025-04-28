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
                        Prodotto prodotto = new Prodotto.Builder()
                                .nome(rs.getString("nome"))
                                .descrizione(rs.getString("descrizione"))
                                .quantita(rs.getInt("quantita"))
                                .prezzo(rs.getDouble("prezzo"))
                                .indirizzo(rs.getString("indirizzo"))
                                .certificati(certificati)
                                .foto(foto)
                                .creatoDa(rs.getString("creato_da"))
                                .stato(stato)
                                .commento(commento)
                                .build();


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

                    Prodotto prodotto = new Prodotto.Builder()
                            .nome(rs.getString("nome"))
                            .descrizione(rs.getString("descrizione"))
                            .quantita(rs.getInt("quantita"))
                            .prezzo(rs.getDouble("prezzo"))
                            .indirizzo(rs.getString("indirizzo"))
                            .certificati(certificati)
                            .foto(foto)
                            .creatoDa(rs.getString("creato_da"))
                            .stato(stato)
                            .commento(commento)
                            .build();

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

                        Prodotto prodotto = new Prodotto.Builder()
                                .nome(rs.getString("nome"))
                                .descrizione(rs.getString("descrizione"))
                                .quantita(rs.getInt("quantita"))
                                .prezzo(rs.getDouble("prezzo"))
                                .indirizzo(rs.getString("indirizzo"))
                                .certificati(certificati)
                                .foto(foto)
                                .creatoDa(rs.getString("creato_da"))
                                .stato(stato)
                                .commento(commento)
                                .build();

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

    public Prodotto getProdottoByNome(String nome) {
        String query = "SELECT * FROM prodotti WHERE nome = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                List<String> certificati = List.of(rs.getString("certificati").split(","));
                List<String> foto = List.of(rs.getString("foto").split(","));
                String commento = rs.getString("commento");

                return new Prodotto.Builder()
                        .nome(rs.getString("nome"))
                        .descrizione(rs.getString("descrizione"))
                        .quantita(rs.getInt("quantita"))
                        .prezzo(rs.getDouble("prezzo"))
                        .indirizzo(rs.getString("indirizzo"))
                        .certificati(certificati)
                        .foto(foto)
                        .creatoDa(rs.getString("creato_da"))
                        .stato(StatoProdotto.valueOf(rs.getString("stato")))
                        .commento(commento)
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean salvaDettagli(Prodotto prodotto) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                        INSERT INTO prodotti 
                            (nome, descrizione, quantita, prezzo, indirizzo, certificati, foto, creato_da, stato, commento)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, prodotto.getNome());
                stmt.setString(2, prodotto.getDescrizione());
                stmt.setInt(3, prodotto.getQuantita());
                stmt.setDouble(4, prodotto.getPrezzo());
                stmt.setString(5, prodotto.getIndirizzo());
                stmt.setString(6, ""); // placeholder, aggiornati dopo con uploadFile()
                stmt.setString(7, "");
                stmt.setString(8, prodotto.getCreatoDa());
                stmt.setString(9, prodotto.getStato().name());
                stmt.setNull(10, Types.VARCHAR);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore invio dati prodotto: " + e.getMessage());
            return false;
        }
    }

    public boolean salvaFile(List<File> certificati, List<File> foto, Prodotto prodotto) {
        try (Connection conn = DatabaseManager.getConnection()) {
            List<String> nomiCertificati = certificati.stream()
                    .map(file -> copiaFile(file, CERTIFICATI_DIR))
                    .collect(Collectors.toList());

            List<String> nomiFoto = foto.stream()
                    .map(file -> copiaFile(file, FOTO_DIR))
                    .collect(Collectors.toList());

            String sql = "UPDATE prodotti SET certificati = ?, foto = ? WHERE nome = ? AND creato_da = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.join(",", nomiCertificati));
                stmt.setString(2, String.join(",", nomiFoto));
                stmt.setString(3, prodotto.getNome());
                stmt.setString(4, prodotto.getCreatoDa());
                stmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Errore durante salvaFile: " + e.getMessage());
            return false;
        }
    }

    public boolean aggiungiInListaApprovazioni(Prodotto prodotto) {
        return aggiornaStatoProdotto(prodotto, StatoProdotto.IN_ATTESA);
    }
}
