package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.BillDto;
import payup.payup.model.Bill;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting between Bill entity and BillDto.
 */
@Component
public class BillMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Converts a Bill entity to a BillDto.
     *
     * @param bill The Bill entity to convert.
     * @return The corresponding BillDto, or null if the input is null.
     */
    public BillDto toDto(Bill bill) {
        if (bill == null) return null;
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setBillType(bill.getBillType());
        dto.setAmount(bill.getAmount());
        dto.setDueDate(bill.getDueDate() != null ? bill.getDueDate().format(FORMATTER) : null);
        dto.setPaid(bill.isPaid());
        dto.setStatus(bill.getStatus() != null ? bill.getStatus().name() : null);
        dto.setOverdue(bill.isOverdue());
        return dto;
    }

    /**
     * Converts a BillDto to a Bill entity.
     *
     * @param dto The BillDto to convert.
     * @return The corresponding Bill entity, or null if the input is null.
     */
    public Bill toEntity(BillDto dto) {
        if (dto == null) return null;
        Bill bill = new Bill();
        bill.setId(dto.getId());
        bill.setBillType(dto.getBillType());
        bill.setAmount(dto.getAmount());
        if (dto.getDueDate() != null) {
            bill.setDueDate(LocalDateTime.parse(dto.getDueDate(), FORMATTER));
        }
        bill.setPaid(dto.isPaid());
        if (dto.getStatus() != null) {
            bill.setStatus(Bill.BillStatus.valueOf(dto.getStatus()));
        }
        return bill;
    }
}