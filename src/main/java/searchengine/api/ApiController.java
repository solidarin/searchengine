package searchengine.api;


import exceptions.UnknownIndexingStatusException;
import searchengine.services.IndexingStatusException;
import searchengine.services.IndexingStatusResponse;
import searchengine.services.search.SearchException;
import searchengine.services.search.SearchRequest;
import searchengine.services.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import searchengine.services.search.SearchService;

import javax.annotation.Resource;
import java.io.IOException;

@RestController()
@RequestMapping("/api")
public class ApiController {

    @Resource
    IndexingService indexingService;
    @Resource
    StatisticsService statisticsService;
    @Resource
    SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> calculateStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingStatusResponse> startIndexing() throws IOException {
        IndexingStatusResponse status = indexingService.startIndexing();
        if (indexingService.isIndexing()) {
            return ResponseEntity.ok(status);
        }
        throw new UnknownIndexingStatusException("Неизвестная ошибка индексирования");
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingStatusResponse> stopIndexing() {
        if (!indexingService.isIndexing()) {
            throw new IndexingStatusException("Индексация не запущена");
        }
        IndexingStatusResponse status = indexingService.stopIndexing();
        if (!indexingService.isIndexing()) {
            return ResponseEntity.ok(status);
        }

        throw new UnknownIndexingStatusException("Неизвестная ошибка индексирования");
    }

    @PostMapping("/indexSite")
    public ResponseEntity<IndexingStatusResponse> indexSite(@RequestParam String siteUrl) throws IOException {
        if (indexingService.isSiteIndexing(siteUrl)) {
            throw new IndexingStatusException("Индексация для сайта " + siteUrl + " уже запущена");
        }
        IndexingStatusResponse status = indexingService.indexSite(siteUrl);
        if (indexingService.isIndexing()) {
            return ResponseEntity.ok(status);
        }

        throw new UnknownIndexingStatusException("Неизвестная ошибка индексирования");
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingStatusResponse> indexPage(@RequestParam String url) throws IOException {
        IndexingStatusResponse status = indexingService.indexOnePage(url);
        if (indexingService.isIndexing()) {
            return ResponseEntity.ok(status);
        }
        throw new UnknownIndexingStatusException("Неизвестная ошибка индексирования");
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam(name = "query") String query,
                                                 @RequestParam(name = "site", required = false) String site,
                                                 @RequestParam(name = "offset", defaultValue = "0") int offset,
                                                 @RequestParam(name = "limit", defaultValue = "20") int limit) throws IOException {
        if (query.isEmpty()) {
            throw new SearchException("Пустой поисковый запрос");
        }

        SearchRequest request = new SearchRequest(query, site, offset, limit);
        Logger log = LoggerFactory.getLogger(ApiController.class);
        log.info(request.toString());
        return ResponseEntity.ok(searchService.search(request));
    }

}
