package payup.payup.dto;

import lombok.Data;
import payup.payup.model.Floor;

import java.util.List;

@Data
public class FloorDto {
    private Long id;
    private String floorName;
    private Long propertyId;
    private Integer floorNumber;
    private List<Long> roomIds;

    public FloorDto(Floor floor) {
    }

    public FloorDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public List<Long> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Long> roomIds) {
        this.roomIds = roomIds;
    }
}
