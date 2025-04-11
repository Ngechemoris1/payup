package payup.payup.mapper;

import org.springframework.stereotype.Component;
import payup.payup.dto.FloorDto;
import payup.payup.model.Floor;

@Component
public class FloorMapper {

    public FloorDto toDto(Floor floor) {
        if (floor == null) {
            return null;
        }

        FloorDto dto = new FloorDto();
        dto.setId(floor.getId());
        dto.setFloorName(floor.getFloorName());
        dto.setFloorNumber(floor.getFloorNumber());
        if (floor.getProperty() != null) {
            dto.setPropertyId(floor.getProperty().getId());
        }
        return dto;
    }

    public Floor toEntity(FloorDto dto) {
        if (dto == null) {
            return null;
        }

        Floor floor = new Floor();
        floor.setId(dto.getId());
        floor.setFloorName(dto.getFloorName());
        floor.setFloorNumber(dto.getFloorNumber());
        // Property should be set separately via service
        return floor;
    }
}