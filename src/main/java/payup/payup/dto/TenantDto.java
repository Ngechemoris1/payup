package payup.payup.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TenantDto {
    private Long id;
    // Removed: name, email, phone
    private Double balance;
    private String floor;

    private UserDto user;

    @JsonManagedReference
    private RoomDto room;

    @JsonManagedReference
    private PropertyDto property;

    @JsonManagedReference
    private List<RentDto> rents;

    @JsonManagedReference
    private List<BillDto> bills;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}