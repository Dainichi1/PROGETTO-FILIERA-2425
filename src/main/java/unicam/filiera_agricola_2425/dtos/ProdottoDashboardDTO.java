package unicam.filiera_agricola_2425.dtos;

import unicam.filiera_agricola_2425.models.CertificatoProdotto;
import unicam.filiera_agricola_2425.models.ImmagineProdotto;
import unicam.filiera_agricola_2425.models.Prodotto;
import java.util.List;

public class ProdottoDashboardDTO {

    private Long id;
    private String nome;
    private double prezzo;
    private int quantita;
    private String descrizione;
    private String commentoRifiuto;
    private List<String> immagini;
    private List<String> certificati;

    // ✅ Metodo statico per la conversione (MODEL → DTO)
    public static ProdottoDashboardDTO fromModel(Prodotto prodotto) {
        ProdottoDashboardDTO dto = new ProdottoDashboardDTO();
        dto.setId(prodotto.getId());
        dto.setNome(prodotto.getNome());
        dto.setPrezzo(prodotto.getPrezzo());
        dto.setQuantita(prodotto.getQuantita());
        dto.setDescrizione(prodotto.getDescrizione());
        dto.setCommentoRifiuto(prodotto.getCommentoRifiuto());

        dto.setImmagini(
                prodotto.getImmagini().stream()
                        .map(ImmagineProdotto::getFileName)
                        .toList()
        );

        dto.setCertificati(
                prodotto.getCertificazioni().stream()
                        .map(CertificatoProdotto::getFileName)
                        .toList()
        );

        return dto;
    }

    // ✅ Getters & Setters (puoi usare Lombok se preferisci)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPrezzo() { return prezzo; }
    public void setPrezzo(double prezzo) { this.prezzo = prezzo; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getCommentoRifiuto() { return commentoRifiuto; }
    public void setCommentoRifiuto(String commentoRifiuto) { this.commentoRifiuto = commentoRifiuto; }

    public List<String> getImmagini() { return immagini; }
    public void setImmagini(List<String> immagini) { this.immagini = immagini; }

    public List<String> getCertificati() { return certificati; }
    public void setCertificati(List<String> certificati) { this.certificati = certificati; }
}
