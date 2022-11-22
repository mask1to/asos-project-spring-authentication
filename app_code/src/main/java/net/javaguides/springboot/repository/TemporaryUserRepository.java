package net.javaguides.springboot.repository;

import net.javaguides.springboot.model.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {

    TemporaryUser findByEmail(String email);

    @Transactional
    Integer deleteTemporaryUserByEmail(String email);
}
