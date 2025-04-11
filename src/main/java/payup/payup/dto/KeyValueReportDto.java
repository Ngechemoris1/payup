package payup.payup.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO representing a key-value report (e.g., tenant ID to amount or property ID to value).
 */
@Data
public class KeyValueReportDto {
    private Map<Long, Double> data;

    public KeyValueReportDto(Map<Long, Double> data) {
        this.data = data;
    }
}
