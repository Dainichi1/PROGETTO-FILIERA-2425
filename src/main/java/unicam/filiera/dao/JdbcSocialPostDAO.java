package unicam.filiera.dao;

import unicam.filiera.dao.SocialPostDAO;
import unicam.filiera.dto.PostSocialDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSocialPostDAO implements SocialPostDAO {

    private final Connection conn;

    public JdbcSocialPostDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void pubblicaPost(PostSocialDto post) {
        String sql = """
                INSERT INTO social_posts
                (autore_username, id_acquisto, nome_item, tipo_item, titolo, testo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, post.getAutoreUsername());
            if (post.getIdAcquisto() != null) {
                ps.setLong(2, post.getIdAcquisto());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, post.getNomeItem());
            ps.setString(4, post.getTipoItem());
            ps.setString(5, post.getTitolo());
            ps.setString(6, post.getTesto());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento del post social", e);
        }
    }

    @Override
    public List<PostSocialDto> findAllOrderByDataDesc() {
        String sql = "SELECT * FROM social_posts ORDER BY created_at DESC";
        List<PostSocialDto> result = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei post social", e);
        }
        return result;
    }

    @Override
    public List<PostSocialDto> findByAutore(String username) {
        String sql = "SELECT * FROM social_posts WHERE autore_username = ? ORDER BY created_at DESC";
        List<PostSocialDto> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei post dell'autore", e);
        }
        return result;
    }


    // Metodo privato per mappare il ResultSet in DTO
    private PostSocialDto mapRow(ResultSet rs) throws SQLException {
        PostSocialDto dto = new PostSocialDto();
        dto.setId(rs.getLong("id"));
        dto.setAutoreUsername(rs.getString("autore_username"));
        int idAcq = rs.getInt("id_acquisto");
        dto.setIdAcquisto(rs.wasNull() ? null : idAcq);
        dto.setNomeItem(rs.getString("nome_item"));
        dto.setTipoItem(rs.getString("tipo_item"));
        dto.setTitolo(rs.getString("titolo"));
        dto.setTesto(rs.getString("testo"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            dto.setCreatedAt(ts.toLocalDateTime());
        }
        return dto;
    }
}
