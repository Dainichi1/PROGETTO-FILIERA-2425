package unicam.filiera_agricola_2425.factories;

import unicam.filiera_agricola_2425.models.Curatore;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;

public class CuratoreFactory implements UtenteFactory {
    @Override
    public UtenteAutenticato creaUtente(String nome, String username, String password) {
        Curatore c = new Curatore();
        c.setNome(nome);
        c.setUsername(username);
        c.setPassword(password);
        return c;
    }
}
