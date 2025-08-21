package unicam.progetto_filiera_springboot.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "pacchetti",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_pacchetti_nome_creatore",
                columnNames = {"nome", "creato_da"}
        )
)
public class Pacchetto implements Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔒 Optimistic Lock
    @Version
    private Long version;

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
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzo;

    @Size(max = 255)
    @Column(length = 255)
    private String indirizzo;

    @Lob
    private String certificati;

    @Lob
    private String foto;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "creato_da",
            referencedColumnName = "username",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pacchetti_creato_da")
    )
    private Utente creatoDa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoPacchetto stato = StatoPacchetto.IN_ATTESA;

    @Size(max = 500)
    @Column(length = 500)
    private String commento;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "pacchetti_prodotti",
            joinColumns = @JoinColumn(name = "pacchetto_id"),
            inverseJoinColumns = @JoinColumn(name = "prodotto_id")
    )
    private Set<Prodotto> prodotti = new LinkedHashSet<>();

    protected Pacchetto() {}

    public Pacchetto(String nome, String descrizione, int quantita,
                     BigDecimal prezzo, String indirizzo, Utente creatoDa) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.quantita = quantita;
        this.prezzo = prezzo;
        this.indirizzo = indirizzo;
        this.creatoDa = creatoDa;
        this.stato = StatoPacchetto.IN_ATTESA;
    }

    // ====== Item getters ======
    @Override public Long getId() { return id; }
    @Override public String getNome() { return nome; }
    @Override public String getDescrizione() { return descrizione; }
    @Override public BigDecimal getPrezzo() { return prezzo; }
    @Override public int getQuantita() { return quantita; }
    @Override public String getIndirizzo() { return indirizzo; }
    @Override public String getCertificati() { return certificati; }
    @Override public String getFoto() { return foto; }

    // ====== setters/getters extra ======
    public void setNome(String nome) { this.nome = nome; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public void setQuantita(int quantita) { this.quantita = quantita; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public void setCertificati(String certificati) { this.certificati = certificati; }
    public void setFoto(String foto) { this.foto = foto; }
    public Utente getCreatoDa() { return creatoDa; }
    public void setCreatoDa(Utente creatoDa) { this.creatoDa = creatoDa; }
    public StatoPacchetto getStato() { return stato; }
    public void setStato(StatoPacchetto stato) { this.stato = stato; }
    public String getCommento() { return commento; }
    public void setCommento(String commento) { this.commento = commento; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Prodotto> getProdotti() { return prodotti; }
    public void setProdotti(Set<Prodotto> prodotti) { this.prodotti = prodotti; }
    public Long getVersion() { return version; }
}
