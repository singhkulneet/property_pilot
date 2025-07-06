package property_pilot.repository;

import property_pilot.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Expense entity.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a given property.
     */
    List<Expense> findByPropertyId(Long propertyId);

    /**
     * Find all expenses in a date range.
     */
    List<Expense> findByDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

}
