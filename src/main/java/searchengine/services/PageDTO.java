package searchengine.services;

public interface PageDTO {
    String getSiteUrl();
    String getSiteName();
    String getPath();
    String getContent();
    double getRelevance();

}
