package unicam.filiera_agricola_2425.factories;

import unicam.filiera_agricola_2425.models.Produttore;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;

public class ProduttoreFactory implements UtenteFactory {
    @Override
    public UtenteAutenticato creaUtente(String nome, String username, String password) {
        Produttore p = new Produttore();
        p.setNome(nome);
        p.setUsername(username);
        p.setPassword(password);
        return p;
    }
}
