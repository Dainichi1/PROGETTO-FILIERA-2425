package unicam.filiera_agricola_2425.dtos;

import java.util.List;

public class ProdottoMarketplaceDTO {
    private Long id;
    private String nome;
    private double prezzo;
    private int quantita;
    private String descrizione;
    private String produttoreNome;
    private List<String> immagini;
    private List<String> certificati;

    // Costruttore da entità Prodotto
    public ProdottoMarketplaceDTO(unicam.filiera_agricola_2425.models.Prodotto prodotto) {
        this.id = prodotto.getId();
        this.nome = prodotto.getNome();
        this.prezzo = prodotto.getPrezzo();
        this.quantita = prodotto.getQuantita();
        this.descrizione = prodotto.getDescrizione();
        this.produttoreNome = prodotto.getProduttore().getNome();
        this.immagini = prodotto.getImmagini().stream()
                .map(img -> img.getFileName())
                .toList();
        this.certificati = prodotto.getCertificazioni().stream()
                .map(cert -> cert.getFileName())
                .toList();
    }

    // Getters e Setters (puoi usare Lombok se vuoi)
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public double getPrezzo() { return prezzo; }
    public int getQuantita() { return quantita; }
    public String getDescrizione() { return descrizione; }
    public String getProduttoreNome() { return produttoreNome; }
    public List<String> getImmagini() { return immagini; }
    public List<String> getCertificati() { return certificati; }
}
