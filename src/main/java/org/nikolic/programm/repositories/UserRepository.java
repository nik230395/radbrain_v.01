package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Suche User per E-Mail
    Optional<User> findByEmail(String email);

    // Prüfen ob E-Mail existiert (für Registrierung)
    boolean existsByEmail(String email);

    // Prüfen ob Vollname existiert (für Registrierung) - angepasst an das Feld `fullname` in User
    boolean existsByFullname(String fullname);
}