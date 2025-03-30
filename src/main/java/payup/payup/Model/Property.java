package payup.payup.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String location;
    private int units;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonBackReference(value = "user-properties") // Prevents infinite loop from User to Property
    private User owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "property-tenants") // Serializes tenants but not back to property
    private List<Tenant> tenants;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Floor> floors; // List of floors in the property

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LandlordBill> landlordBills; // List of bills associated with the property

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Property() {
        this.tenants = new ArrayList<>();
        this.floors = new ArrayList<>();
        this.landlordBills = new ArrayList<>();
    }

    public Property(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.tenants = new ArrayList<>();
        this.floors = new ArrayList<>();
        this.landlordBills = new ArrayList<>();
    }

    public Property(String name, String type, String location, int units, User owner) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.units = units;
        this.owner = owner;
        this.tenants = new ArrayList<>();
        this.floors = new ArrayList<>();
        this.landlordBills = new ArrayList<>();
    }

}