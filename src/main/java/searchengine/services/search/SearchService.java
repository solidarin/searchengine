package searchengine.services.search;



import searchengine.services.PageDTO;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.model.Site;
import searchengine.services.CommonContext;
import searchengine.services.LemmaUtils;
import searchengine.services.MorphologyService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static CommonContext commonContext;

    public SearchService(CommonContext commonContext) {
        this.commonContext = commonContext;
    }

    private static final double THRESHOLD = 0.97;

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public static SearchResponse search(SearchRequest request) throws IOException, SearchException {

        String[] searchWordsNormalForms;
        List<Site> sitesToSearch;
        List<PageDTO> foundPages;
        List<String> filteredLemmas;
        String message = "";

        long searchStartTime = System.nanoTime();

        sitesToSearch = getSitesToSearch(request.getSite());

        searchWordsNormalForms = LemmaUtils.getAndCountNormalFormsInString(request.getQuery(), commonContext.getMorphologyService()).keySet().toArray(new String[0]);

        if (searchWordsNormalForms.length == 0) {
            throw new SearchException("Не удалось выделить леммы для поиска из запроса");
        }

        filteredLemmas = commonContext.getDatabaseService().filterPopularLemmasOut(sitesToSearch, List.of(searchWordsNormalForms), THRESHOLD);

        if (filteredLemmas.size() == 0) {
            throw new SearchException("По запросу '" + request.getQuery() + "' ничего не найдено");
        }

        foundPages = findRelevantPages(filteredLemmas, sitesToSearch, request.getLimit(), request.getOffset());

        if (foundPages.size() == 0) {
            throw new SearchException("По запросу '" + request.getQuery() + "' ничего не найдено");
        }

        String finalQuery = request.getQuery();
        if (filteredLemmas.size() < searchWordsNormalForms.length) {
            finalQuery = correctQuery(filteredLemmas, finalQuery);
            message = "По запросу '" + request.getQuery() + "' ничего не найдено. " +
                    "Скорректированный запрос: '" + finalQuery + "'.";
            log.info(message);
        }

        double maxRelevance = foundPages.get(0).getRelevance();
        ForkJoinPool pool = ForkJoinPool.commonPool();
        PageProcessor pp = new PageProcessor(
                foundPages, List.of(commonContext.getMorphologyService().splitStringToLowercaseWords(finalQuery)),
                maxRelevance, 4, commonContext.getMorphologyService()
        );
        List<FoundPage> searchResults = pool.submit(pp).join();

        log.info("search for request \"" + request.getQuery() + "\" complete, found " + searchResults.size() + " pages");
        return new SearchResponse(
                true,
                message + String.format(" Время поиска : %.3f сек.", (System.nanoTime() - searchStartTime)/1000000000.),
                searchResults.size(),
                searchResults
        );
    }

    static List<Site> getSitesToSearch(String site) {
        if (site == null) {
            return commonContext.getDatabaseService().getAllSites();
        } else {
            return new ArrayList<>() {{
                add(commonContext.getDatabaseService().getSiteByUrl(site));
            }};
        }
    }

    static List<PageDTO> findRelevantPages(List<String> filteredLemmas, List<Site> sitesToSearch, int limit, int offset) {
        List<PageDTO> foundPages;
        do {
            foundPages = commonContext.getDatabaseService().getSortedRelevantPageDTOs(filteredLemmas,
                    sitesToSearch.stream().map(Site::getId).collect(Collectors.toList()), limit, offset);
            if (foundPages.size() > 0) {
                break;
            }

            filteredLemmas.remove(0);
        } while (filteredLemmas.size() > 0);

        return foundPages;
    }

    static String correctQuery(List<String> lemmas, String originalQuery) {
        MorphologyService ms = commonContext.getMorphologyService();

        String[] splitQuery = ms.splitStringToWords(originalQuery);
        List<String> queryList = new ArrayList<>(List.of(splitQuery));
        List<String> wordNormalForms;

        for (String word : splitQuery) {
            wordNormalForms = ms.getNormalFormOfAWord(word.toLowerCase(Locale.ROOT));
            if (wordNormalForms.isEmpty()) {
                queryList.remove(word);
                continue;
            }
            if (!lemmasContainAnyWordNormalForm(wordNormalForms, lemmas)) {
                queryList.remove(word);
            }
        }
        return String.join(" ", queryList);
    }

    static boolean lemmasContainAnyWordNormalForm(List<String> wordNormalForms, List<String> lemmas) {
        List<String> lemmasWordIntersection = new ArrayList<String>(lemmas);
        lemmasWordIntersection.retainAll(wordNormalForms);
        return !lemmasWordIntersection.isEmpty();
    }
}