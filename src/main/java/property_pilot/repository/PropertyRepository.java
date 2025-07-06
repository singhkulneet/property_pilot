package property_pilot.repository;

import property_pilot.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Property entity.
 */
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
}
