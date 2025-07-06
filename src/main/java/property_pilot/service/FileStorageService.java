package property_pilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Handles saving receipt files to disk.
 */
@Service
public class FileStorageService {

    @Value("${property_pilot.receipts.base-dir}")
    private String baseDir;

    public String storeFile(Long propertyId, Long expenseId, MultipartFile file) throws IOException {
        // Build the directory path
        Path dirPath = Path.of(baseDir, propertyId.toString(), expenseId.toString());

        // Create directories if needed
        Files.createDirectories(dirPath);

        // Clean file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Destination path
        Path targetPath = dirPath.resolve(fileName);

        // Save the file
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path for saving in the DB
        return propertyId + "/" + expenseId + "/" + fileName;
    }
}
