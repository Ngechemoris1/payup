package payup.payup.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a property in the PayUp system. This class is an entity 
 * mapped to the "properties" table in the database.
 */
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property {
    
    /**
     * The unique identifier for the property.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name or description of the property.
     */
    private String name;

    /**
     * The owner of the property establishing a many-to-one relationship 
     * with the User entity, where one user can own multiple properties.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * List of tenants living in this property.  establishes a one-to-many 
     * relationship with the Tenant entity, where one property can have multiple tenants.
     */
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tenant> tenants;

    /**
     * Timestamp for when the property was created. Automatically set by JPA auditing.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Timestamp for when the property was last updated. Automatically set by JPA auditing.
     */
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors

    /**
     * Default constructor for JPA.
     */
    public Property() {
        this.tenants = new ArrayList<>();
    }

    /**
     * Constructor with name and owner, used when creating a new property.
     * 
     * @param name The name or description of the property.
     * @param owner The User object representing the property's owner.
     */
    public Property(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.tenants = new ArrayList<>();
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Tenant> getTenants() {
        return tenants;
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
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

    /**
     * Adds a new tenant to this property by specifying their floor and room.
     * 
     * @param tenant The tenant to be added.
     * @param floor The floor where the tenant resides.
     * @param room The room number or identifier for the tenant.
     * @throws IllegalArgumentException if the room is already occupied.
     */
    public void addTenant(Tenant tenant, String floor, String room) {
        if (tenant == null || floor == null || room == null) {
            throw new IllegalArgumentException("Tenant, floor, and room must not be null");
        }
        
        if (isRoomOccupied(floor, room)) {
            throw new IllegalArgumentException("The room " + room + " on floor " + floor + " is already occupied");
        }

        tenant.setFloor(floor);
        tenant.setRoom(room);
        tenant.setProperty(this);
        if (!this.tenants.contains(tenant)) {
            this.tenants.add(tenant);
        }
    }

    /**
     * Removes a tenant from this property.
     * 
     * @param tenant The tenant to be removed from this property.
     * @return true if the tenant was present and removed, false otherwise.
     */
    public boolean removeTenant(Tenant tenant) {
        if (tenant != null && this.tenants.remove(tenant)) {
            tenant.setProperty(null);  // Disassociates the tenant from this property
            tenant.setFloor(null);
            tenant.setRoom(null);
            return true;
        }
        return false;
    }
}   