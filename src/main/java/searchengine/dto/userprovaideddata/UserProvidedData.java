package searchengine.dto.userprovaideddata;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import searchengine.services.SiteUrlAndNameDTO;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "search-engine-properties.preload")
@Data
public class UserProvidedData {
    private List<SiteUrlAndNameDTO> sites;
    private List<FieldDTO> fields;
    private List<AuthDetailsDTO> authorisations;
}
