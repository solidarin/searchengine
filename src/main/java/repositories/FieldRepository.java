package repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Field;

public interface FieldRepository extends JpaRepository<Field, Long> {
    boolean existsByName(String name);
}
