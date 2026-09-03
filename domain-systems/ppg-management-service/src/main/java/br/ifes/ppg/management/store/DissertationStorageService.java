package br.ifes.ppg.management.store;

import br.ifes.ppg.management.domain.DissertationDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DissertationStorageService {

    private final Path storageDirectory;

    public DissertationStorageService(
            @Value("${ppg.documents-dir:data/documents}") String documentsDir) {
        this.storageDirectory = Path.of(documentsDir);
    }

    public StoredFile store(Long defenseId, String documentId, MultipartFile file) {
        try {
            Files.createDirectories(storageDirectory);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null
                    ? "dissertation"
                    : file.getOriginalFilename());
            String extension = extension(originalName);
            Path target = storageDirectory.resolve(documentId + extension).normalize();
            if (!target.startsWith(storageDirectory.toAbsolutePath().normalize())
                    && !target.startsWith(storageDirectory.normalize())) {
                throw new IllegalArgumentException("Invalid dissertation file name");
            }
            file.transferTo(target.toFile());
            return new StoredFile(defenseId, originalName, target.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Could not store dissertation file", e);
        }
    }

    public Resource load(DissertationDocument document) {
        try {
            Path path = Path.of(document.getStoragePath()).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Dissertation file is not readable: " + document.getDocumentId());
            }
            return resource;
        } catch (Exception e) {
            throw new IllegalStateException("Could not load dissertation file", e);
        }
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot);
    }

    public record StoredFile(Long defenseId, String originalName, String storagePath) {
    }
}
