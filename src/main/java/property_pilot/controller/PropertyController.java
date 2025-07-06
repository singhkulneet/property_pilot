package property_pilot.controller;

import property_pilot.model.Property;
import property_pilot.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST endpoints to manage properties.
 */
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyRepository propertyRepository;

    // Get all properties
    @GetMapping
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    // Get one property by ID
    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id) {
        Optional<Property> prop = propertyRepository.findById(id);
        return prop.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Create new property
    @PostMapping
    public Property createProperty(@RequestBody Property property) {
        return propertyRepository.save(property);
    }

    // Update property
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(@PathVariable Long id, @RequestBody Property updatedProperty) {
        return propertyRepository.findById(id).map(prop -> {
            prop.setName(updatedProperty.getName());
            prop.setAddress(updatedProperty.getAddress());
            prop.setNotes(updatedProperty.getNotes());
            return ResponseEntity.ok(propertyRepository.save(prop));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete property
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        if (propertyRepository.existsById(id)) {
            propertyRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
