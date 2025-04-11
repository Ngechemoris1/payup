package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.PropertyCreateRequestDto;
import payup.payup.dto.PropertyDto;
import payup.payup.dto.PropertyUpdateRequestDto;
import payup.payup.model.Property;

@Component
public class PropertyMapper {

    public PropertyDto toDto(Property property) {
        if (property == null) {
            return null;
        }

        PropertyDto dto = new PropertyDto();
        dto.setId(property.getId());
        dto.setName(property.getName());
        dto.setType(property.getType());
        dto.setLocation(property.getLocation());
        dto.setUnits(property.getUnits());
        if (property.getOwner() != null) {
            dto.setOwnerId(property.getOwner().getId());
        }
        return dto;
    }

    public Property toEntity(PropertyCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Property property = new Property();
        property.setId(dto.getId());
        property.setName(dto.getName());
        property.setType(dto.getType());
        property.setLocation(dto.getLocation());
        property.setUnits(dto.getUnits());
        // Owner will be set by the service layer using landlordId
        return property;
    }

    public Property toEntity(PropertyUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Property property = new Property();
        property.setId(dto.getId());
        property.setName(dto.getName());
        property.setType(dto.getType());
        property.setLocation(dto.getLocation());
        property.setUnits(dto.getUnits());
        // Owner is not updated here; handled by service if needed
        return property;
    }

    public Property toEntity(PropertyDto dto) {
        if (dto == null) {
            return null;
        }

        Property property = new Property();
        property.setId(dto.getId());
        property.setName(dto.getName());
        property.setType(dto.getType());
        property.setLocation(dto.getLocation());
        property.setUnits(dto.getUnits());
        // Owner should be set by the service layer if needed (dto.getOwnerId() available)
        return property;
    }
}