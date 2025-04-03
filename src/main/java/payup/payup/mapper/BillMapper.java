package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.BillDto;
import payup.payup.model.Bill;

@Component
public class BillMapper {

    public BillDto toDto(Bill bill) {
        if (bill == null) {
            return null;
        }

        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setBillType(bill.getBillType());
        dto.setAmount(bill.getAmount());
        dto.setDueDate(bill.getDueDate());
        dto.setPaid(bill.isPaid());
        dto.setStatus(bill.getStatus().name());
        dto.setOverdue(bill.isOverdue());

        if (bill.getTenant() != null) {
            dto.setTenantId(bill.getTenant().getId());
        }

        return dto;
    }

    public Bill toEntity(BillDto dto) {
        if (dto == null) {
            return null;
        }

        Bill bill = new Bill();
        bill.setId(dto.getId());
        bill.setBillType(dto.getBillType());
        bill.setAmount(dto.getAmount());
        bill.setDueDate(dto.getDueDate());
        bill.setPaid(dto.isPaid());
        if (dto.getStatus() != null) {
            bill.setStatus(Bill.BillStatus.valueOf(dto.getStatus()));
        }
        // Tenant should be set separately via service
        return bill;
    }
}
