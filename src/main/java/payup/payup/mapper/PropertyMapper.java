package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.PropertyDto;
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
        // Owner should be set separately via service
        return property;
    }
}
