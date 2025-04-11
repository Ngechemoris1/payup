package payup.payup.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class PropertyCreateRequestDto {
    private Long id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Type is mandatory")
    private String type;
    @NotBlank(message = "Location is mandatory")
    private String location;
    @NotNull(message = "Units are mandatory")
    private Integer units;
    @NotNull(message = "Landlord ID is mandatory")
    private Long landlordId;
    @NotNull(message = "Number of floors is mandatory")
    private Integer numberOfFloors;
}