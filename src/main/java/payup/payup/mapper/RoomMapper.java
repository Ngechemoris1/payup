package payup.payup.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import payup.payup.dto.RoomDto;
import payup.payup.model.Room;

@Component
public class RoomMapper {

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private PropertyMapper propertyMapper;

    public RoomDto toDto(Room room) {
        if (room == null) {
            return null;
        }

        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRentAmount(room.getRentAmount());
        dto.setOccupied(room.isOccupied());

        if (room.getFloor() != null) {
            dto.setFloor(floorMapper.toDto(room.getFloor()));
        }
        if (room.getProperty() != null) {
            dto.setProperty(propertyMapper.toDto(room.getProperty()));
        }
        return dto;
    }

    public Room toEntity(RoomDto dto) {
        if (dto == null) {
            return null;
        }

        Room room = new Room();
        room.setId(dto.getId());
        room.setRoomNumber(dto.getRoomNumber());
        room.setRentAmount(dto.getRentAmount());
        room.setOccupied(dto.isOccupied());
        // Relationships should be set separately via service
        return room;
    }
}
