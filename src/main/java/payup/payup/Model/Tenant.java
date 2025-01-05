package payup.payup.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a tenant in the PayUp system. This class is an entity 
 * mapped to the "tenants" table in the database.
 */
@Entity
@Table(name = "tenants")
@EntityListeners(AuditingEntityListener.class)
public class Tenant {
    
    /**
     * The unique identifier for the tenant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The tenant's name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The tenant's email address, which should be unique.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The tenant's phone number.
     */
    private String phone;

    /**
     * The property where the tenant resides.
     * This establishes a many-to-one relationship with the Property entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    /**
     * The floor where the tenant's unit is located.
     */
    private String floor;

    /**
     * The room or unit number of the tenant.
     */
    private String room;

    /**
     * List of rent records associated with this tenant.
     * This establishes a one-to-many relationship with the Rent entity.
     */
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rent> rents;

    /**
     * Timestamp for when the tenant was created. Automatically set by JPA auditing.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Timestamp for when the tenant was last updated. Automatically set by JPA auditing.
     */
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors

    /**
     * Default constructor for JPA.
     */
    public Tenant() {
    }

    /**
     * Constructor to create a new tenant with basic information.
     * 
     * @param name The name of the tenant.
     * @param email The email address of the tenant.
     * @param phone The phone number of the tenant.
     * @param property The property where the tenant lives.
     * @param floor The floor where the tenant's unit is located.
     * @param room The room or unit number.
     */
    public Tenant(String name, String email, String phone, Property property, String floor, String room) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.property = property;
        this.floor = floor;
        this.room = room;
    }

    // Getters and Setters

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

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<Rent> getRents() {
        return rents;
    }

    public void setRents(List<Rent> rents) {
        this.rents = rents;
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

    // Custom methods

    /**
     * Adds a new rent record to this tenant.
     * 
     * @param rent The rent record to be added.
     */
    public void addRent(Rent rent) {
        if (rent != null) {
            rent.setTenant(this);
            if (!this.rents.contains(rent)) {
                this.rents.add(rent);
            }
        }
    }

    /**
     * Removes a rent record from this tenant.
     * 
     * @param rent The rent record to be removed.
     * @return true if the rent was successfully removed, false otherwise.
     */
    public boolean removeRent(Rent rent) {
        if (rent != null && this.rents.remove(rent)) {
            rent.setTenant(null);
            return true;
        }
        return false;
    }

    // Override toString for better logging and debugging
    @Override
    public String toString() {
        return "Tenant{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", floor='" + floor + '\'' +
               ", room='" + room + '\'' +
               ", property=" + (property != null ? property.getId() : "null") +
               ", rentCount=" + (rents != null ? rents.size() : 0) +
               '}';
    }
}