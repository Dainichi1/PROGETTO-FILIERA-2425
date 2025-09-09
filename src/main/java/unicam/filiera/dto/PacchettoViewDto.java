package unicam.filiera.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PacchettoViewDto {

    private Long id;
    private String nome;
    private String descrizione;
    private int quantita;
    private double prezzo;
    private String indirizzo;
    private String creatoDa;
    private String stato;
    private String commento;

    private List<String> certificati;
    private List<String> foto;

    private List<String> prodottiNomi;
}
