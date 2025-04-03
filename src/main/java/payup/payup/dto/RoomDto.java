package payup.payup.dto;

import payup.payup.model.Room;

public class RoomDto {
    private Long id;
    private int roomNumber;

    public RoomDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public FloorDto getFloor() {
        return floor;
    }

    public void setFloor(FloorDto floor) {
        this.floor = floor;
    }

    private double rentAmount;
    private boolean occupied;
    private FloorDto floor;

    public RoomDto(Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.rentAmount = room.getRentAmount();
        this.occupied = room.isOccupied();
        this.floor = new FloorDto(room.getFloor());
    }

    public void setProperty(PropertyDto dto) {
    }

}
