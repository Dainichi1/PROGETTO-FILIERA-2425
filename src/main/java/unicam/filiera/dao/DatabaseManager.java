package unicam.filiera.dao;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/filiera;DB_CLOSE_DELAY=-1";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, "sa", "");
        }
        return connection;
    }

    public static void checkAndUpdateDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Controlla se la colonna "indirizzo" esiste già
            ResultSet rs = conn.getMetaData().getColumns(null, null, "PRODOTTI", "INDIRIZZO");
            if (!rs.next()) { // Se NON esiste
                System.out.println("[DB] Colonna 'indirizzo' non trovata. Aggiornamento struttura tabella...");
                String alterSql = "ALTER TABLE prodotti ADD COLUMN indirizzo VARCHAR(255) DEFAULT NULL;";
                stmt.executeUpdate(alterSql);
                System.out.println("[DB] Colonna 'indirizzo' aggiunta con successo.");
            } else {
                System.out.println("[DB] Colonna 'indirizzo' già presente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Tabella utenti (già presente)
            String utentiSql = """
                CREATE TABLE IF NOT EXISTS utenti (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(50),
                    nome VARCHAR(50),
                    cognome VARCHAR(50),
                    ruolo VARCHAR(30)
                );
            """;

            // Nuova tabella prodotti
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

            stmt.executeUpdate(utentiSql);
            stmt.executeUpdate(prodottiSql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
