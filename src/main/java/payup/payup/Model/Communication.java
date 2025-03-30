package payup.payup.model;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "messages")
@Data
public class Communication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public enum MessageType {
        SMS, EMAIL
    }

    public enum MessageStatus {
        SENT, DELIVERED, READ
    }
}
