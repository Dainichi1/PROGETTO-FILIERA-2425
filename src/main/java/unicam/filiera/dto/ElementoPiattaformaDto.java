package unicam.filiera.dto;

import java.sql.Timestamp;

public class ElementoPiattaformaDto {
    private final String id;        // può essere ID numerico o username
    private final String nome;      // nome/titolo/descrizione breve
    private final String tipo;      // categoria o tipo elemento
    private final String stato;     // stato/stato_pagamento ecc.
    private final Timestamp data;   // data_ora / data_inizio / created_at
    private final String extra;     // info sintetiche addizionali

    public ElementoPiattaformaDto(String id, String nome, String tipo, String stato, Timestamp data, String extra) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.stato = stato;
        this.data = data;
        this.extra = extra;
    }
    public String getId()   { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getStato(){ return stato; }
    public Timestamp getData(){ return data; }
    public String getExtra(){ return extra; }
}
