package payup.payup.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import payup.payup.dto.TenantDto;
import payup.payup.model.Tenant;

@Component
public class TenantMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private RoomMapper roomMapper;

    public TenantDto toDto(Tenant tenant) {
        if (tenant == null) {
            return null;
        }

        TenantDto dto = new TenantDto();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setEmail(tenant.getEmail());
        dto.setPhone(tenant.getPhone());
        dto.setBalance(tenant.getBalance());

        if (tenant.getUser() != null) {
            dto.setUser(userMapper.toDto(tenant.getUser()));
        }
        if (tenant.getProperty() != null) {
            dto.setProperty(propertyMapper.toDto(tenant.getProperty()));
        }
        if (tenant.getRoom() != null) {
            dto.setRoom(roomMapper.toDto(tenant.getRoom()));
        }
        return dto;
    }

    public Tenant toEntity(TenantDto dto) {
        if (dto == null) {
            return null;
        }

        Tenant tenant = new Tenant();
        tenant.setId(dto.getId());
        tenant.setName(dto.getName());
        tenant.setEmail(dto.getEmail());
        tenant.setPhone(dto.getPhone());
        tenant.setBalance(dto.getBalance());
        // Relationships should be set separately via service
        return tenant;
    }
}
