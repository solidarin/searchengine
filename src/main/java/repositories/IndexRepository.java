package repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.Index;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Long> {
    List<Index> findAllByPage_Id(Long pageId);

    @Query(
            value = "select i.page_id " +
                    "from index i " +
                    "join lemma l on l.id = i.lemma_id " +
                    "where l.site_id in :siteIds " +
                    "and l.lemma = :lemma " +
                    "and i.page_id in (:pageIds)",
            nativeQuery = true
    )
    List<Long> findPageIdsBySiteInAndLemmaAndPageIdsIn(
            List<Long> siteIds,
            String lemma,
            List<Long> pageIds);
}

