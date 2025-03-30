package payup.payup.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * Represents a room in a property.
 * A room can be assigned to a tenant and belongs to a floor.
 */
@Data
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the room

    @Column(nullable = false, unique = true)
    private int roomNumber; // Room number (e.g., 101 for floor 1, room 1)

    @Column(nullable = false)
    private boolean isOccupied; // Indicates if the room is occupied by a tenant

    @Column(nullable = false)
    private double rentAmount; // Rent amount for the room

    @ManyToOne
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor; // Floor to which the room belongs

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property; // generates setProperty() and getProperty()

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tenant> tenants; // List of tenants assigned to the room

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }
}
