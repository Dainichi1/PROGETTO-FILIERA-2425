package unicam.filiera;

import unicam.filiera.dao.DatabaseManager;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Prodotto;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        //  Inizializza il database: crea le tabelle se non esistono
        DatabaseManager.initDatabase();
        DatabaseManager.checkAndUpdateDatabase();


        System.out.println("Utenti registrati nel sistema:");
        for (UtenteAutenticato u : UtenteDAO.getInstance().getTuttiGliUtenti()) {
            System.out.println("- " + u);
        }

        System.out.println("Prodotti registrati nel sistema:");
        for (Prodotto p : new ProdottoDAO().getTuttiIProdotti()) {
            System.out.println(p);
        }

        // Lancia GUI
        SwingUtilities.invokeLater(() -> {
            unicam.filiera.view.MainWindow window = new unicam.filiera.view.MainWindow();
            window.setVisible(true);
        });
    }
}
