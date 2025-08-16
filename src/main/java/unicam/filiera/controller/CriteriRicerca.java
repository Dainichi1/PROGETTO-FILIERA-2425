package unicam.filiera.controller;

public class CriteriRicerca {
    public String testo;     // ricerca libera su nome/extra
    public String stato;     // se valorizzato, filtra per stato
    public String orderBy;   // "DATA" | "NOME" | "STATO"
    public boolean asc = true;
}
