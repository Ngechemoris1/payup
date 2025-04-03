package payup.payup.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import payup.payup.model.Tenant;

import java.time.LocalDateTime;
import java.util.List;

public class TenantDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double balance;

    @JsonUnwrapped // Flattens user details into the response
    private UserDto user;

    public TenantDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public RoomDto getRoom() {
        return room;
    }

    public void setRoom(RoomDto room) {
        this.room = room;
    }

    public PropertyDto getProperty() {
        return property;
    }

    public void setProperty(PropertyDto property) {
        this.property = property;
    }

    public List<RentDto> getRents() {
        return rents;
    }

    public void setRents(List<RentDto> rents) {
        this.rents = rents;
    }

    public List<BillDto> getBills() {
        return bills;
    }

    public void setBills(List<BillDto> bills) {
        this.bills = bills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    private RoomDto room;
    private PropertyDto property;
    private List<RentDto> rents;
    private List<BillDto> bills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors, getters, setters
    public TenantDto(Tenant tenant) {
        this.id = tenant.getId();
        this.name = tenant.getName();
        this.email = tenant.getEmail();
        this.phone = tenant.getPhone();
        this.balance = tenant.getBalance();
        this.user = new UserDto(tenant.getUser());
        this.room = new RoomDto(tenant.getRoom());
        this.property = new PropertyDto(tenant.getProperty());
        this.rents = tenant.getRents().stream().map(RentDto::new).toList();
        this.bills = tenant.getBills().stream().map(BillDto::new).toList();
        this.createdAt = tenant.getCreatedAt();
        this.updatedAt = tenant.getUpdatedAt();
    }
}
