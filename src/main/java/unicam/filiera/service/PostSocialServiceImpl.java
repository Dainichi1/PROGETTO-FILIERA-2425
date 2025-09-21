package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.EventoTipo;
import unicam.filiera.entity.PostSocialEntity;
import unicam.filiera.factory.ItemFactory;
import unicam.filiera.factory.EventoFactory;
import unicam.filiera.factory.PostSocialFactory;
import unicam.filiera.model.Item;
import unicam.filiera.model.Evento;
import unicam.filiera.model.PostSocial;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.observer.OsservatorePostSocial;
import unicam.filiera.repository.*;
import unicam.filiera.validation.PostValidator;

import java.util.List;

@Service
public class PostSocialServiceImpl implements PostSocialService {

    private final PostSocialRepository postRepo;
    private final ProdottoRepository prodottoRepo;
    private final PacchettoRepository pacchettoRepo;
    private final ProdottoTrasformatoRepository trasformatoRepo;
    private final FieraRepository fieraRepo;
    private final VisitaInvitoRepository visitaRepo;
    private final List<OsservatorePostSocial> observers;
    private final AcquistoRepository acquistoRepo;

    @Autowired
    public PostSocialServiceImpl(PostSocialRepository postRepo,
                                 ProdottoRepository prodottoRepo,
                                 PacchettoRepository pacchettoRepo,
                                 AcquistoRepository acquistoRepo,
                                 ProdottoTrasformatoRepository trasformatoRepo,
                                 FieraRepository fieraRepo,
                                 VisitaInvitoRepository visitaRepo,
                                 List<OsservatorePostSocial> observers) {
        this.postRepo = postRepo;
        this.prodottoRepo = prodottoRepo;
        this.pacchettoRepo = pacchettoRepo;
        this.trasformatoRepo = trasformatoRepo;
        this.fieraRepo = fieraRepo;
        this.visitaRepo = visitaRepo;
        this.observers = observers;
        this.acquistoRepo = acquistoRepo;
    }

    @Override
    @Transactional
    public PostSocialDto pubblicaPost(Long itemId, String autoreUsername, PostSocialDto dto) {
        PostValidator.valida(dto);

        String tipo = dto.getTipoItem();
        if (tipo == null) {
            throw new IllegalArgumentException("⚠ Tipo dell'item mancante");
        }

        PostSocialEntity entity;

        try {
            // --- ITEM ---
            ItemTipo itemTipo = ItemTipo.valueOf(tipo.toUpperCase());
            Item item = ItemFactory.fromId(itemId, itemTipo, prodottoRepo, pacchettoRepo, trasformatoRepo);
            entity = PostSocialFactory.creaPost(dto, item, autoreUsername);
        } catch (IllegalArgumentException ex1) {
            try {
                // --- EVENTO ---
                EventoTipo eventoTipo = EventoTipo.valueOf(tipo.toUpperCase());
                Evento evento = EventoFactory.fromId(itemId, eventoTipo, fieraRepo, visitaRepo);
                entity = PostSocialFactory.creaPost(dto, evento, autoreUsername);
            } catch (IllegalArgumentException ex2) {
                throw new UnsupportedOperationException("Tipo non supportato: " + tipo);
            }
        }

        var saved = postRepo.save(entity);
        PostSocial model = saved.toModel();

        // Notifica agli osservatori
        observers.forEach(obs -> obs.notifica(model, "NUOVO_POST"));

        return PostSocialDto.fromModel(model);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostSocialDto> getAllPosts() {
        return postRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(e -> PostSocialDto.fromModel(e.toModel()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostSocialDto> getPostsByAutore(String username) {
        return postRepo.findByAutoreUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(e -> PostSocialDto.fromModel(e.toModel()))
                .toList();
    }

    @Override
    @Transactional
    public PostSocialDto pubblicaRecensione(Long acquistoId, String autoreUsername, PostSocialDto dto) {
        PostValidator.valida(dto);

        var acquisto = acquistoRepo.findById(acquistoId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Acquisto non trovato"));

        // verifica che l’acquisto appartenga all’autore
        if (!acquisto.getUsernameAcquirente().equals(autoreUsername)) {
            throw new IllegalArgumentException("❌ Non puoi recensire un acquisto non tuo");
        }

// verifica stato pagamento
        if (acquisto.getStatoPagamento() != StatoPagamento.APPROVATO) {
            throw new IllegalArgumentException("❌ Puoi recensire solo acquisti approvati");
        }

        PostSocialEntity entity = PostSocialFactory.creaRecensione(dto, acquisto, autoreUsername);

        var saved = postRepo.save(entity);
        PostSocial model = saved.toModel();

        observers.forEach(obs -> obs.notifica(model, "NUOVA_RECENSIONE"));

        return PostSocialDto.fromModel(model);
    }
}
