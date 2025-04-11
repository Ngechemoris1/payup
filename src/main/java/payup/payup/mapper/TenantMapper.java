package payup.payup.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import payup.payup.dto.*;
import payup.payup.model.Tenant;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class TenantMapper {
    private static final Logger logger = LoggerFactory.getLogger(TenantMapper.class);

    private final PropertyMapper propertyMapper;
    private final RoomMapper roomMapper;
    private final UserMapper userMapper;
    private final RentMapper rentMapper;
    private final BillMapper billMapper;

    @Autowired
    public TenantMapper(PropertyMapper propertyMapper,
                        RoomMapper roomMapper,
                        UserMapper userMapper,
                        RentMapper rentMapper,
                        BillMapper billMapper) {
        this.propertyMapper = propertyMapper;
        this.roomMapper = roomMapper;
        this.userMapper = userMapper;
        this.rentMapper = rentMapper;
        this.billMapper = billMapper;
    }

    public TenantDto toDto(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        TenantDto dto = new TenantDto();
        dto.setId(tenant.getId());
        dto.setBalance(tenant.getBalance());
        dto.setFloor(tenant.getFloor());
        dto.setProperty(propertyMapper.toDto(tenant.getProperty()));
        RoomDto roomDto = roomMapper.toDto(tenant.getRoom());
        if (roomDto != null) {
            roomDto.setTenant(dto);
        }
        dto.setRoom(roomDto);
        dto.setUser(userMapper.toDto(tenant.getUser()));
        dto.setRents(tenant.getRents() != null
                ? tenant.getRents().stream().map(rent -> {
            RentDto rentDto = rentMapper.toDto(rent);
            rentDto.setTenant(dto);
            return rentDto;
        }).collect(Collectors.toList())
                : Collections.emptyList());
        dto.setBills(tenant.getBills() != null
                ? tenant.getBills().stream().map(bill -> {
            BillDto billDto = billMapper.toDto(bill);
            billDto.setTenant(dto);
            return billDto;
        }).collect(Collectors.toList())
                : Collections.emptyList());
        dto.setCreatedAt(tenant.getCreatedAt());
        dto.setUpdatedAt(tenant.getUpdatedAt());
        return dto;
    }

    public Tenant toEntity(TenantCreateDto dto) {
        if (dto == null) {
            return null;
        }
        Tenant tenant = new Tenant();
        tenant.setName(dto.getName());
        tenant.setEmail(dto.getEmail());
        tenant.setPhone(dto.getPhone());
        tenant.setBalance(dto.getBalance() != null ? dto.getBalance() : 0.0);
        tenant.setFloor(dto.getFloor());
        logger.debug("Mapped Tenant from DTO: {}", tenant);
        return tenant;
    }
}