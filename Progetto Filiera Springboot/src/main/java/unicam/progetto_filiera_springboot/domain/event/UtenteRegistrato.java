package unicam.progetto_filiera_springboot.domain.event;

public class UtenteRegistrato implements DomainEvent {
    private final String username;

    public UtenteRegistrato(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
