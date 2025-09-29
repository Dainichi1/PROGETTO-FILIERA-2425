-- Creazione utente di default se non esiste già
MERGE INTO UTENTI KEY(USERNAME)
    VALUES ('default_user', 'Default', NULL, 'Utente', 'password', 'PRODUTTORE');
