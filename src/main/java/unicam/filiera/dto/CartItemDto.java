package unicam.filiera.dto;

public class CartItemDto {
    private final String tipo;
    private final String nome;
    private final int quantita;
    private final double prezzoUnitario;
    private final double totale;

    public CartItemDto(String tipo, String nome, int quantita, double prezzoUnitario) {
        this.tipo          = tipo;
        this.nome          = nome;
        this.quantita      = quantita;
        this.prezzoUnitario= prezzoUnitario;
        this.totale        = prezzoUnitario * quantita;
    }

    public String getTipo()           { return tipo; }
    public String getNome()           { return nome; }
    public int    getQuantita()       { return quantita; }
    public double getPrezzoUnitario() { return prezzoUnitario; }
    public double getTotale()         { return totale; }
}
