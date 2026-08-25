package service;

import dao.ImageDAO;
import model.ComplaintImage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles complaint image uploads: validation, copying the real file into
 * uploads/ with a generated unique name (Java NIO), and saving its metadata
 * via ImageDAO. MySQL never stores the image bytes themselves - see spec #17.
 */
public class ImageService {

    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final String UPLOAD_DIR = "uploads";

    private final ImageDAO imageDAO = new ImageDAO();

    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }

    /** Checks extension and size before anything touches disk. Call this right after JFileChooser returns. */
    public void validate(Path sourceFile) throws ValidationException, IOException {
        if (sourceFile == null || !Files.isRegularFile(sourceFile) || !Files.isReadable(sourceFile)) {
            throw new ValidationException("Please select a readable image file.");
        }
        String name = sourceFile.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
            throw new ValidationException("Only .jpg, .jpeg or .png images are allowed.");
        }
        long size = Files.size(sourceFile);
        if (size > MAX_SIZE_BYTES) {
            throw new ValidationException("Image size must be less than 5 MB.");
        }
        if (size == 0) {
            throw new ValidationException("The selected image file is empty or unreadable.");
        }
        BufferedImage image = ImageIO.read(sourceFile.toFile());
        if (image == null) {
            throw new ValidationException("The selected file is not a valid image.");
        }
    }

    /**
     * Copies the file into uploads/ with a collision-proof name
     * (e.g. CMP1001_BEFORE_3f2a91.jpg), then records it in MySQL.
     */
    public ComplaintImage store(Path sourceFile, String complaintId, ComplaintImage.Kind kind)
            throws IOException, SQLException, ValidationException {

        validate(sourceFile);
        Path uploadDirectory = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);

        String original = sourceFile.getFileName().toString();
        String ext = original.substring(original.lastIndexOf('.'));
        String uniqueName = complaintId + "_" + kind.name() + "_" +
                UUID.randomUUID().toString().substring(0, 8) + ext;

        Path target = uploadDirectory.resolve(uniqueName).normalize();
        if (!target.getParent().equals(uploadDirectory)) {
            throw new IOException("Invalid upload path.");
        }
        Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);

        ComplaintImage img = new ComplaintImage(
            complaintId,
            original,
            target.toString(),
            ext.replace(".", "").toUpperCase(Locale.ROOT),
            Files.size(target),
            kind
        );
        try {
            int id = imageDAO.insert(img);
            img.setImageId(id);
            return img;
        } catch (SQLException | RuntimeException ex) {
            Files.deleteIfExists(target);
            throw ex;
        }
    }
}
