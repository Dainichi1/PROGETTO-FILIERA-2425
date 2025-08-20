package unicam.progetto_filiera_springboot.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "prodotti",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_prodotti_nome_creatore",
                columnNames = {"nome", "creato_da"}
        )
)
public class Prodotto implements Item{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descrizione;

    @PositiveOrZero
    @Column(nullable = false)
    private int quantita;

    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzo;

    @Size(max = 255)
    @Column(length = 255)
    private String indirizzo;

    @Lob
    private String certificati; // CSV (es: "cert1.pdf,cert2.pdf")

    @Lob
    private String foto;        // CSV (es: "img1.jpg,img2.jpg")

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "creato_da",
            referencedColumnName = "username",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_prodotti_creato_da")
    )
    private Utente creatoDa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoProdotto stato = StatoProdotto.IN_ATTESA;

    @Size(max = 500)
    @Column(length = 500)
    private String commento;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Prodotto() {
    }

    public Prodotto(String nome, String descrizione, int quantita, BigDecimal prezzo, String indirizzo, Utente creatoDa) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.indirizzo = indirizzo;
        this.creatoDa = creatoDa;
        this.stato = StatoProdotto.IN_ATTESA;
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCertificati() {
        return certificati;
    }

    public void setCertificati(String certificati) {
        this.certificati = certificati;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Utente getCreatoDa() {
        return creatoDa;
    }

    public void setCreatoDa(Utente creatoDa) {
        this.creatoDa = creatoDa;
    }

    public StatoProdotto getStato() {
        return stato;
    }

    public void setStato(StatoProdotto stato) {
        this.stato = stato;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
