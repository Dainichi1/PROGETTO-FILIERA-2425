package unicam.filiera.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/filiera;DB_CLOSE_DELAY=-1";

    /**
     * Restituisce una nuova Connection ad ogni chiamata,
     * evitando di condividere una connessione statica.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, "sa", "");
    }

    /**
     * Verifica e aggiunge la colonna 'indirizzo' se mancante.
     */
    public static void checkAndUpdateDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = conn.getMetaData()
                    .getColumns(null, null, "PRODOTTI", "INDIRIZZO");
            if (!rs.next()) {
                System.out.println("[DB] Aggiungo colonna 'indirizzo' alla tabella prodotti");
                stmt.executeUpdate(
                        "ALTER TABLE prodotti ADD COLUMN indirizzo VARCHAR(255) DEFAULT NULL"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inizializza le tabelle del database se non esistono.
     */
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String utentiSql = """
                CREATE TABLE IF NOT EXISTS utenti (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(50),
                    nome VARCHAR(50),
                    cognome VARCHAR(50),
                    ruolo VARCHAR(30)
                );
            """;

            String prodottiSql = """
                CREATE TABLE IF NOT EXISTS prodotti (
                    id IDENTITY PRIMARY KEY,
                    nome VARCHAR(100),
                    descrizione VARCHAR(500),
                    quantita INT,
                    prezzo DOUBLE,
                    indirizzo VARCHAR(255),
                    certificati TEXT,
                    foto TEXT,
                    creato_da VARCHAR(50),
                    stato VARCHAR(20) DEFAULT 'IN_ATTESA',
                    commento VARCHAR(255) DEFAULT NULL
                );
            """;

            String pacchettiSql = """
                CREATE TABLE IF NOT EXISTS pacchetti (
                    id IDENTITY PRIMARY KEY,
                    nome VARCHAR(100),
                    descrizione VARCHAR(500),
                    indirizzo VARCHAR(255),
                    prezzo_totale DOUBLE,
                    prodotti TEXT,
                    certificati TEXT,
                    foto TEXT,
                    creato_da VARCHAR(50),
                    stato VARCHAR(20) DEFAULT 'IN_ATTESA',
                    commento VARCHAR(255) DEFAULT NULL
                );
            """;

            stmt.executeUpdate(utentiSql);
            stmt.executeUpdate(prodottiSql);
            stmt.executeUpdate(pacchettiSql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
