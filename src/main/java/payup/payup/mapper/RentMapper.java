package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.RentDto;
import payup.payup.model.Rent;

import java.math.BigDecimal;

@Component
public class RentMapper {

    public RentDto toDto(Rent rent) {
        if (rent == null) {
            return null;
        }

        RentDto dto = new RentDto();
        dto.setId(rent.getId());
        dto.setAmount(rent.getAmount());
        dto.setDueDate(rent.getDueDate());
        dto.setPaid(rent.isPaid());
        dto.setNotes(rent.getNotes());
        dto.setOverdue(rent.isOverdue());
        if (rent.getTenant() != null) {
            dto.setTenantId(rent.getTenant().getId());
        }
        return dto;
    }

    public Rent toEntity(RentDto dto) {
        if (dto == null) {
            return null;
        }

        Rent rent = new Rent();
        rent.setId(dto.getId());
        rent.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
        rent.setDueDate(dto.getDueDate());
        rent.setPaid(dto.isPaid());
        rent.setNotes(dto.getNotes());
        // Tenant should be set separately via service
        return rent;
    }
}