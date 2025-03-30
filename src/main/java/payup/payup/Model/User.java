package payup.payup.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.validation.constraints.NotEmpty;
import payup.payup.model.User.UserRole;

import java.util.ArrayList;
import java.util.List;

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

    public enum UserRole {
        ADMIN, LANDLORD, TENANT
    }

    public User() {
        this.properties = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return firstName + " " + lastName; }
    public void setName(String name) {
        String[] parts = name.split(" ", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }
    private String firstName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
        public String getLastName() {
            return lastName;
        }
    
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public List<Property> getProperties() { return properties; }
    public void setProperties(List<Property> properties) { this.properties = properties; }

    public void encodePassword(String rawPassword, BCryptPasswordEncoder encoder) {
        this.password = encoder.encode(rawPassword);
    }
}