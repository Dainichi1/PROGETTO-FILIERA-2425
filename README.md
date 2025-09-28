# Filiera Agricola 24/25

Applicazione web realizzata con **Spring Boot** e **Java** per la gestione digitale di una filiera agricola.  
Consente la gestione di utenti, prodotti, pacchetti e marketplace, integra funzionalità social e una mappa interattiva.

---

## Tecnologie Utilizzate

L'applicazione utilizza le seguenti tecnologie e librerie principali:

### Backend
- **Java 22**
- **Spring Boot 3.5.4**
    - `spring-boot-starter` – Core e auto-configurazione
    - `spring-boot-starter-web` – Gestione MVC e controller web
    - `spring-boot-starter-data-jpa` – Persistenza dati (JPA + Hibernate)
    - `spring-boot-starter-security` – Autenticazione e autorizzazione utenti
    - `spring-boot-starter-thymeleaf` – Template engine per pagine dinamiche
    - `spring-boot-starter-validation` – Validazione dati lato server
    - `spring-boot-devtools` – Ricaricamento automatico in fase di sviluppo
- **Hibernate** – ORM per JPA
- **H2 Database (file-based)** – Database relazionale embedded persistente
- **Lombok** – Riduzione del boilerplate Java (getter/setter, builder)
- **JMapViewer (OpenStreetMap)** – Supporto alle mappe interattive lato Java

### Frontend
- **Thymeleaf** – Template engine HTML con supporto per espressioni e binding dei dati
- **Leaflet.js** – Libreria JS per mappe interattive
- HTML5, CSS3, JavaScript – UI responsive e interattiva

### Build e Test
- **Maven** – Gestione delle dipendenze e build
- `spring-boot-starter-test` – Test unitari e d’integrazione
- `spring-security-test` – Test di sicurezza

---

## Database

L'applicazione utilizza un database **H2 persistente** basato su file locale.

**JDBC URL:**
jdbc:h2:file:./db-data/filiera_agricola


- Console H2 attiva su: `http://localhost:8080/h2-console`
- Dialect: `org.hibernate.dialect.H2Dialect`
- Configurazione automatica dello schema: `spring.jpa.hibernate.ddl-auto=update`

---

## Ruoli e Funzionalità

Gli utenti possono registrarsi scegliendo un ruolo.  
Ogni ruolo dispone di funzionalità specifiche:

### Utente Generico
- Registrazione con scelta ruolo: Produttore, Trasformatore, Distributore, Acquirente, Curatore, Animatore, Gestore
- Login
- Visualizzazione:
    - **Marketplace**
    - **Social Network**
    - **Mappa interattiva**

---

### Produttore
- Visualizzare social network
- Visualizzare e prenotare visite disponibili
- Cancellare prenotazioni
- Richiedere eliminazione profilo
- Creare prodotti e inviarli al Curatore per approvazione
- Eliminare prodotti (**solo in attesa o rifiutati**)
- Modificare prodotti (**solo rifiutati**)
- Pubblicare annunci sul social network (**solo approvati**)
- Visualizzare la mappa

---

### Trasformatore
- Tutte le funzionalità del Produttore
- Creare **prodotti trasformati** inserendo **almeno 2 fasi di produzione**
- Eliminare prodotti trasformati (**solo in attesa o rifiutati**)
- Modificare prodotti trasformati (**solo rifiutati**)

---

### Distributore
- Visualizzare social network
- Visualizzare e prenotare visite disponibili
- Cancellare prenotazioni
- Richiedere eliminazione profilo
- Creare **pacchetti trasformati** contenenti **almeno 2 prodotti**
- Inviare pacchetti al Curatore per approvazione
- Eliminare pacchetti (**solo in attesa o rifiutati**)
- Modificare pacchetti (**solo rifiutati**)
- Pubblicare annunci sul social network (**solo approvati**)
- Visualizzare la mappa

---

### Curatore
- Richiedere eliminazione profilo
- Visualizzare social network
- **Approvare o rifiutare**:
    - Prodotti
    - Pacchetti
    - Prodotti trasformati
- Visualizzare la mappa

---

### Acquirente
- Aggiornare **fondi disponibili**
- Visualizzare:
    - Social network
    - Marketplace
    - Carrello
    - Acquisti
    - Fiere disponibili
    - Ingressi prenotati alle fiere
- Richiedere eliminazione profilo
- **Acquistare** prodotti dal marketplace e ingressi alle fiere
- **Eliminare ingressi acquistati**
- Pubblicare recensioni sul social network (**solo per oggetti acquistati**)
- Visualizzare la mappa

---

### Animatore
- Richiedere eliminazione profilo
- Visualizzare social network
- Creare **visite ad invito**
- Creare **fiere**
- Pubblicare annunci sul social network
- Visualizzare la mappa

---

### Gestore della Piattaforma
- Visualizzare il contenuto della piattaforma
- Richiedere eliminazione profilo
- Visualizzare social network
- **Accettare o rifiutare** richieste di eliminazione profilo
- Aggiungere **marker** sulla mappa
- Visualizzare la mappa

---



