package unicam.filiera.dao;

import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.DatiAcquistoDto;

import java.sql.*;
import java.util.List;

public class JdbcAcquistoDAO implements AcquistoDAO {

    @Override
    public int salvaAcquisto(DatiAcquistoDto dati) {
        String insertAcquisto = """
                INSERT INTO acquisti (
                    username_acquirente,
                    totale,
                    stato_pagamento,
                    tipo_metodo_pagamento,
                    data_ora,
                    fondi_pre_acquisto,
                    fondi_post_acquisto,
                    elenco_item
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String insertItem = """
                INSERT INTO acquisto_items (
                    id_acquisto,
                    nome_item,
                    tipo_item,
                    quantita,
                    prezzo_unitario,
                    totale
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        Connection conn = null;
        PreparedStatement psAcquisto = null;
        PreparedStatement psItem = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Inserisci riga in ACQUISTI
            psAcquisto = conn.prepareStatement(insertAcquisto, Statement.RETURN_GENERATED_KEYS);
            psAcquisto.setString(1, dati.getUsernameAcquirente());
            psAcquisto.setDouble(2, dati.getTotaleAcquisto());
            psAcquisto.setString(3, dati.getStatoPagamento().name());
            psAcquisto.setString(4, dati.getTipoMetodoPagamento().name());
            psAcquisto.setTimestamp(5, Timestamp.valueOf(dati.getTimestamp()));
            psAcquisto.setDouble(6, dati.getFondiPreAcquisto());
            psAcquisto.setDouble(7, dati.getFondiPostAcquisto());

            // Serializzazione semplice degli item (es: nomi separati da virgola)
            String elencoItem = dati.getItems().stream()
                    .map(i -> i.getNome() + " x" + i.getQuantita())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            psAcquisto.setString(8, elencoItem);

            psAcquisto.executeUpdate();

            // Ottieni l'ID generato
            generatedKeys = psAcquisto.getGeneratedKeys();
            if (!generatedKeys.next()) {
                conn.rollback();
                return -1;
            }
            int idAcquisto = generatedKeys.getInt(1);

            // 2. Inserisci tutti gli item associati
            psItem = conn.prepareStatement(insertItem);
            for (CartItemDto item : dati.getItems()) {
                psItem.setInt(1, idAcquisto);
                psItem.setString(2, item.getNome());
                psItem.setString(3, item.getTipo());
                psItem.setInt(4, item.getQuantita());
                psItem.setDouble(5, item.getPrezzoUnitario());
                psItem.setDouble(6, item.getTotale());
                psItem.addBatch();
            }
            psItem.executeBatch();

            conn.commit();
            return idAcquisto;
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ignore) {
            }
            return -1;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
            } catch (Exception ignore) {
            }
            try {
                if (psItem != null) psItem.close();
            } catch (Exception ignore) {
            }
            try {
                if (psAcquisto != null) psAcquisto.close();
            } catch (Exception ignore) {
            }
            try {
                if (conn != null) conn.close();
            } catch (Exception ignore) {
            }
        }
    }

}
