package payup.payup.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PropertyDto {
    private Long id;
    private String name;
    private String type;
    private String location;
    private int units;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonBackReference // Prevents recursion back to tenants
    private List<TenantDto> tenants;
}