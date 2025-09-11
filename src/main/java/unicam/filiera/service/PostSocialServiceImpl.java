package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.factory.ItemFactory;
import unicam.filiera.factory.PostSocialFactory;
import unicam.filiera.model.Item;
import unicam.filiera.model.PostSocial;
import unicam.filiera.observer.OsservatorePostSocial;
import unicam.filiera.repository.PostSocialRepository;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoTrasformatoRepository;
import unicam.filiera.validation.PostValidator;

import java.util.List;

@Service
public class PostSocialServiceImpl implements PostSocialService {

    private final PostSocialRepository postRepo;
    private final ProdottoRepository prodottoRepo;
    private final PacchettoRepository pacchettoRepo;
    private final ProdottoTrasformatoRepository trasformatoRepo;
    private final List<OsservatorePostSocial> observers;

    @Autowired
    public PostSocialServiceImpl(PostSocialRepository postRepo,
                                 ProdottoRepository prodottoRepo,
                                 PacchettoRepository pacchettoRepo,
                                 ProdottoTrasformatoRepository trasformatoRepo,
                                 List<OsservatorePostSocial> observers) {
        this.postRepo = postRepo;
        this.prodottoRepo = prodottoRepo;
        this.pacchettoRepo = pacchettoRepo;
        this.trasformatoRepo = trasformatoRepo;
        this.observers = observers;
    }

    @Override
    @Transactional
    public PostSocialDto pubblicaPost(Long itemId, String autoreUsername, PostSocialDto dto) {
        // Recupera l’item dal DB
        Item item = ItemFactory.fromId(itemId, prodottoRepo, pacchettoRepo, trasformatoRepo);

        // Valida titolo e testo
        PostValidator.valida(dto);

        // Costruisci l’entità con dati utente + dati item dal DB
        var entity = PostSocialFactory.creaPost(dto, item, autoreUsername);

        // Salva
        var saved = postRepo.save(entity);

        // Converto in modello
        PostSocial model = saved.toModel();

        // Notifica tutti gli observer registrati
        observers.forEach(obs -> obs.notifica(model, "NUOVO_POST"));

        // Ritorno DTO dal model
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
}
