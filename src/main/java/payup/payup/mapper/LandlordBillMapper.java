package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.LandlordBillDto;
import payup.payup.model.LandlordBill;

@Component
public class LandlordBillMapper {

    public LandlordBillDto toDto(LandlordBill bill) {
        if (bill == null) {
            return null;
        }
        LandlordBillDto dto = new LandlordBillDto();
        dto.setId(bill.getId());
        dto.setType(bill.getType());
        dto.setAmount(bill.getAmount());
        dto.setDueDate(bill.getDueDate());
        dto.setOverdue(bill.getDueDate().isBefore(java.time.LocalDate.now()));
        if (bill.getLandlord() != null) {
            dto.setLandlordId(bill.getLandlord().getId());
        }
        if (bill.getProperty() != null) {
            dto.setPropertyId(bill.getProperty().getId());
        }
        return dto;
    }

    public LandlordBill toEntity(LandlordBillDto dto) {
        if (dto == null) {
            return null;
        }
        LandlordBill bill = new LandlordBill();
        bill.setId(dto.getId());
        bill.setType(dto.getType());
        bill.setAmount(dto.getAmount());
        bill.setDueDate(dto.getDueDate());
        bill.setLandlordId(dto.getLandlordId());
        bill.setPropertyId(dto.getPropertyId());
        return bill;
    }
}