package repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    User findByUsername(String username);
}
