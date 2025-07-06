package property_pilot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents an expense (rent, mortgage, HOA, etc.) for a property.
 */
@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many expenses belong to one property
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    // Date of the expense
    @Column(nullable = false)
    private LocalDate date;

    // Expense category: rent, mortgage, hoa, maintenance
    @Column(nullable = false)
    private String category;

    // Amount spent
    @Column(nullable = false)
    private BigDecimal amount;

    // Optional description
    private String description;

    // Path to receipt file, if any
    private String receiptPath;
}
