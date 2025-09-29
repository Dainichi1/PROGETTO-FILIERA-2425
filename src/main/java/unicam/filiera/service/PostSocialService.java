package unicam.filiera.service;

import unicam.filiera.dto.PostSocialDto;

import java.util.List;

public interface PostSocialService {

    PostSocialDto pubblicaPost(Long itemId, String autoreUsername, PostSocialDto dto);

    PostSocialDto pubblicaRecensione(Long acquistoId, String autoreUsername, PostSocialDto dto);

    List<PostSocialDto> getAllPosts();

    List<PostSocialDto> getPostsByAutore(String username);
}
