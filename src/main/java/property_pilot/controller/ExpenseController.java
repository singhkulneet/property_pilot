package property_pilot.controller;

import property_pilot.model.Expense;
import property_pilot.model.Property;
import property_pilot.repository.ExpenseRepository;
import property_pilot.repository.PropertyRepository;
import property_pilot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST endpoints to manage expenses.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Get all expenses
    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    // Get expenses by property
    @GetMapping("/property/{propertyId}")
    public List<Expense> getExpensesByProperty(@PathVariable Long propertyId) {
        return expenseRepository.findByPropertyId(propertyId);
    }

    // Get expenses in date range
    @GetMapping("/range")
    public List<Expense> getExpensesInRange(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return expenseRepository.findByDateBetween(startDate, endDate);
    }

    // Create a new expense
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        // Ensure the property exists
        Optional<Property> propertyOpt = propertyRepository.findById(expense.getProperty().getId());
        if (propertyOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Reattach property reference
        expense.setProperty(propertyOpt.get());

        // Save expense
        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.ok(saved);
    }

    // Delete an expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Upload a receipt to an expense
    @PostMapping("/{id}/upload")
    public ResponseEntity<String> uploadReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        // Check expense exists
        Optional<Expense> expenseOpt = expenseRepository.findById(id);
        if (expenseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Expense expense = expenseOpt.get();

        try {
            // Store file
            String relativePath = fileStorageService.storeFile(
                    expense.getProperty().getId(),
                    expense.getId(),
                    file);

            // Save the receipt path
            expense.setReceiptPath(relativePath);
            expenseRepository.save(expense);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to store file: " + e.getMessage());
        }
    }
}
