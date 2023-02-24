package repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;
import searchengine.model.Status;

public interface SiteRepository extends JpaRepository<Site, Long> {
    boolean existsByUrl(String url);
    boolean existsByStatus(Status status);
    Site findByUrl(String url);
}
