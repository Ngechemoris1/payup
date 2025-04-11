package payup.payup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PropertyUpdateRequestDto {
    private Long id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Type is mandatory")
    private String type;
    @NotBlank(message = "Location is mandatory")
    private String location;
    @NotNull(message = "Units are mandatory")
    private Integer units;
}