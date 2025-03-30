package payup.payup.model;
import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Represents a floor within a property in the GoldenProperties system.
 * A floor belongs to a single property and contains multiple rooms.
 */
@Data
@Entity
@Table(name = "floors")
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the floor

    @Column(nullable = false)
    @NotNull(message = "Floor name cannot be null")
    private String floorName; // Name of the floor (e.g., "Ground Floor", "Floor 1")

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property; // Property to which the floor belongs

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms; // List of rooms on this floor

    @Column(nullable = false)
    @NotNull(message = "Floor number cannot be null")
    private Integer floorNumber; // Number of the floor (e.g., 0 for Ground Floor, 1 for First Floor)

    // Constructor
    public Floor(Property property, String floorName, Integer floorNumber) {
        this.property = property;
        this.floorName = floorName;
        this.floorNumber = floorNumber;
    }

    // No-argument constructor
    public Floor() {
    }
}
