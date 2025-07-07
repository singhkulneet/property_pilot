package property_pilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import property_pilot.model.Expense;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Handles saving receipt files to disk.
 */
@Service
public class FileStorageService {

    @Value("${property_pilot.receipts.base-dir}")
    private String baseDir;

    /**
     * Stores a file under /{propertyId}_{propertySlug}/{expenseId}_{expenseSlug}/
     * Returns the relative path to store in the DB.
     */
    public String storeFile(Expense expense, MultipartFile file) throws IOException {
        // Generate slugs
        String propertySlug = toSlug(expense.getProperty().getName());
        String expenseSlug = toSlug(expense.getCategory() + "_" + expense.getDate().toString());

        // Build directory path
        Path dirPath = Path.of(
                baseDir,
                expense.getProperty().getId() + "_" + propertySlug,
                expense.getId() + "_" + expenseSlug
        );

        // Create directories if needed
        Files.createDirectories(dirPath);

        // Clean filename to prevent path traversal and nested folders
        String filenameOnly = Path.of(file.getOriginalFilename()).getFileName().toString();
        String fileName = StringUtils.cleanPath(filenameOnly);

        // Destination path
        Path targetPath = dirPath.resolve(fileName);

        // Save the file
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return relative path (e.g., "1_Main_Street_Duplex/2_July_Mortgage/receipt.pdf")
        return expense.getProperty().getId() + "_" + propertySlug + "/"
                + expense.getId() + "_" + expenseSlug + "/"
                + fileName;
    }

    /**
     * Converts text to a filesystem-safe slug.
     */
    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        // Allow underscores and hyphens
        slug = slug.replaceAll("[^a-zA-Z0-9\\s_-]", ""); // note the _ and - inside the brackets
        slug = slug.trim().replaceAll("\\s+", "_");
        return slug;
    }
}
