package searchengine.services.crawler;


import crawlercommons.robots.BaseRobotRules;
import lombok.Data;
import searchengine.model.Field;
import searchengine.model.Site;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class CrawlerContext {
    private Site site;
    private ForkJoinPool thisPool;
    private Set<String> visitedPages;
    private AtomicInteger numberOfPagesToCrawl;
    private Set<Field> fields;
    private Random delayGenerator;
    private BaseRobotRules robotsRules;

    private boolean reindexOnePage;

    public CrawlerContext(Site site, ForkJoinPool thisPool, int pagesToCrawlLimit, Set<Field> fields, BaseRobotRules robotsRules) {
        this.site = site;
        this.thisPool = thisPool;
        this.fields = fields;
        this.robotsRules = robotsRules;

        this.visitedPages = ConcurrentHashMap.newKeySet();
        this.numberOfPagesToCrawl = new AtomicInteger(pagesToCrawlLimit);
        this.delayGenerator = new Random(System.currentTimeMillis());
    }
}
