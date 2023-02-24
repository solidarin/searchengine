package searchengine.dto.statistics;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Status;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Detailed {
    private String url;
    private String name;
    private Status status;
    private long statusTime;
    private String error;
    private long pages;
    private long lemmas;
}
