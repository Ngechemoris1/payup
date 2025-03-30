package payup.payup.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenants")
@EntityListeners(AuditingEntityListener.class)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonBackReference(value = "property-tenants") // Prevents serialization back to property
    private Property property;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Associated user account for the tenant

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // Room assigned to the tenant

    @Column(nullable = false)
    private Double balance; // Tracks overpayment (negative) or underpayment (positive)

    private String floor;
    private String roomNumber; // Renamed to avoid conflict with Room entity

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rent> rents;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bill> bills; // List of bills associated with the tenant

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Tenant() {
        this.rents = new ArrayList<>();
        this.bills = new ArrayList<>();
    }

    public Tenant(String name, String email, String phone, Property property, User user, Room room, Double balance, String floor, String roomNumber) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.property = property;
        this.user = user;
        this.room = room;
        this.balance = balance;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.rents = new ArrayList<>();
        this.bills = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public List<Rent> getRents() { return rents; }
    public void setRents(List<Rent> rents) { this.rents = rents; }
    public List<Bill> getBills() { return bills; }
    public void setBills(List<Bill> bills) { this.bills = bills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addRent(Rent rent) {
        if (rent != null) {
            rent.setTenant(this);
            if (!this.rents.contains(rent)) {
                this.rents.add(rent);
            }
        }
    }

    public boolean removeRent(Rent rent) {
        if (rent != null && this.rents.remove(rent)) {
            rent.setTenant(null);
            return true;
        }
        return false;
    }

    public void addBill(Bill bill) {
        if (bill != null) {
            bill.setTenant(this);
            if (!this.bills.contains(bill)) {
                this.bills.add(bill);
            }
        }
    }

    public boolean removeBill(Bill bill) {
        if (bill != null && this.bills.remove(bill)) {
            bill.setTenant(null);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Tenant{id=" + id + ", name='" + name + "', email='" + email + "', phone='" + phone + "', floor='" + floor + "', roomNumber='" + roomNumber + "', property=" + (property != null ? property.getId() : "null") + ", rentCount=" + (rents != null ? rents.size() : 0) + ", billCount=" + (bills != null ? bills.size() : 0) + "}";
    }
}