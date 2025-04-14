package payup.payup.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import payup.payup.model.Bill;
import payup.payup.model.Property;
import payup.payup.model.User.UserRole;




/**
 * Entity representing a user in the system (e.g., ADMIN, LANDLORD, TENANT).
 */
@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String lastName;

    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid")
    private String email;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "Phone cannot be empty")
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Property> properties;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "user-bills")
    private List<Bill> bills;

    public enum UserRole {
        ADMIN, LANDLORD, TENANT
    }

    private String firstName;

    public User() {
        this.properties = new ArrayList<>();
        this.bills = new ArrayList<>();
    }

    public String getName() { return firstName + " " + lastName; }
    public void setName(String name) {
        String[] parts = name.split(" ", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }

    public void encodePassword(String rawPassword, BCryptPasswordEncoder encoder) {
        this.password = encoder.encode(rawPassword);
    }

    // Explicit getters and setters for bills
    public List<Bill> getBills() { return bills; }
    public void setBills(List<Bill> bills) { this.bills = bills; }
}