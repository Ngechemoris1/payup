package payup.payup.dto;

import lombok.Data;

@Data
public class ErrorResponseDto {
    private String error;
    private String details;

    public ErrorResponseDto(String error, String details) {
        this.error = error;
        this.details = details;
    }
}