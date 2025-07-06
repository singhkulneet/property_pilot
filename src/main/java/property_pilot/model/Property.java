package property_pilot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a property (house, condo, rental unit, etc).
 */
@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Property name (e.g., "Main Street Duplex")
    @Column(nullable = false)
    private String name;

    // Optional address
    private String address;

    // Optional notes
    private String notes;

    // Future: you could add owner info, metadata, etc.
}
