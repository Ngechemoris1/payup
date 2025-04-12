package payup.payup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a tenant in the system.
 */
@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    private String floor;

    private Integer roomId;

    private Double balance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tenant")
    private List<Rent> rents;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Rent> getRents() { return rents; }
    public void setRents(List<Rent> rents) { this.rents = rents; }

    // Methods to match TenantMapper
    public String getName() { return user != null ? user.getName() : null; }
    public void setName(String name) { if (user != null) user.setName(name); }
    public String getEmail() { return user != null ? user.getEmail() : null; }
    public void setEmail(String email) { if (user != null) user.setEmail(email); }
    public String getPhone() { return user != null ? user.getPhone() : null; }
    public void setPhone(String phone) { if (user != null) user.setPhone(phone); }
    public Room getRoom() { return null; } // Adjust if Room relationship exists

    // Access bills via User
    public List<Bill> getBills() { return user != null ? user.getBills() : null; }
}