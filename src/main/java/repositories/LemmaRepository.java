package repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.services.FilteredLemmaDTO;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    long countBySite(Site site);

    List<Lemma> findAllBySiteAndLemmaIn(Site site, List<String> lemmas);

    @Query(
            value = "select " +
                "distinct l.lemma as lemma, " +
                "sum(l.frequency) over (partition by l.lemma) as fr " +
            "from lemma l " +
            "join index i on l.id = i.lemma_id " +
            "where l.site_id in :siteIds " +
            "and l.lemma in :lemmas " +
            "group by l.lemma, l.frequency, l.id " +
            "having count(i.page_id) < (select cast(count(p.id) as double precision) * :threshold from page p) " +
            "order by fr asc",
            nativeQuery = true)
    List<FilteredLemmaDTO> filterVeryPopularLemmas(
            List<Long> siteIds,
            List<String> lemmas,
            double threshold);
}
