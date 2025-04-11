package payup.payup.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO representing a property performance report with multiple metrics.
 */
@Data
public class PropertyPerformanceReportDto {
    private Map<Long, Map<String, Double>> data;

    public PropertyPerformanceReportDto(Map<Long, Map<String, Double>> data) {
        this.data = data;
    }
}
