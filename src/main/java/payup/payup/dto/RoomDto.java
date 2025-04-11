package payup.payup.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    private int roomNumber;
    private double rentAmount;
    private boolean occupied;
    @JsonBackReference
    private FloorDto floor;
    @JsonBackReference
    private PropertyDto property;
    @JsonBackReference
    private TenantDto tenant;
}