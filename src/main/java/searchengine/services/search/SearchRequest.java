package searchengine.services.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    @NotEmpty
    private String query;
    @NotEmpty
    private String site;
    private int offset;
    private int limit;
}
