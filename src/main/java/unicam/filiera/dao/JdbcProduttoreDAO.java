package unicam.filiera.dao;

import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Produttore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcProduttoreDAO implements ProduttoreDAO {
    private static JdbcProduttoreDAO instance;

    private JdbcProduttoreDAO() {
    }

    public static JdbcProduttoreDAO getInstance() {
        if (instance == null) instance = new JdbcProduttoreDAO();
        return instance;
    }

    @Override
    public List<Produttore> findAll() {
        List<Produttore> list = new ArrayList<>();
        String sql = "SELECT username, password, nome, cognome FROM utenti WHERE ruolo = 'PRODUTTORE'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Produttore(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("nome"),
                        rs.getString("cognome")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public List<Prodotto> findProdottiApprovatiByProduttore(String usernameProduttore) {
        List<Prodotto> prodotti = new ArrayList<>();
        String sql = "SELECT * FROM prodotti WHERE creato_da = ? AND stato = 'APPROVATO'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, usernameProduttore);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    prodotti.add(buildProdottoFromRs(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prodotti;
    }

    private Prodotto buildProdottoFromRs(ResultSet rs) throws SQLException {
        // Copia esatta di quello presente in JdbcProdottoDAO
        String certCsv = rs.getString("certificati");
        List<String> cert = (certCsv == null || certCsv.isBlank())
                ? List.of()
                : List.of(certCsv.split(","));

        String fotoCsv = rs.getString("foto");
        List<String> foto = (fotoCsv == null || fotoCsv.isBlank())
                ? List.of()
                : List.of(fotoCsv.split(","));

        return new Prodotto.Builder()
                .nome(rs.getString("nome"))
                .descrizione(rs.getString("descrizione"))
                .quantita(rs.getInt("quantita"))
                .prezzo(rs.getDouble("prezzo"))
                .indirizzo(rs.getString("indirizzo"))
                .certificati(cert)
                .foto(foto)
                .creatoDa(rs.getString("creato_da"))
                .stato(unicam.filiera.model.StatoProdotto.valueOf(rs.getString("stato")))
                .commento(rs.getString("commento"))
                .build();
    }

}
