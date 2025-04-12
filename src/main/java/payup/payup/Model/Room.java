package payup.payup.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "roomNumber"}))
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int roomNumber;

    @Column(nullable = false)
    private boolean isOccupied;

    @Column(nullable = false)
    private double rentAmount;

    @ManyToOne
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }
}