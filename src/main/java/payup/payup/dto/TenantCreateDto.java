package payup.payup.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TenantCreateDto {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Balance is required")
    private Double balance;

    private String floor;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    private Long userId; // Optional - if provided, links to an existing user; if null, creates a new user

    @NotBlank(message = "Password is required when creating a new user")
    private String password;
}