package inshining.virtualstorage.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalStorageService implements StorageService {
    @Value("${storage.location}")
    private String storageLocation;

    @Override
    public InputStream getFileAsInputStream(String storagePath) throws IOException {
        Path path = Paths.get(storageLocation, storagePath);
        return Files.newInputStream(path);
    }

    @Override
    public boolean uploadFile(String storagePath, MultipartFile file) throws IOException {
        Path path = Paths.get(storageLocation, storagePath);
        Files.write(path, file.getBytes());
        return Files.exists(path);
    }

    @Override
    public boolean deleteFile(String storagePath) throws IOException {
        Path path = Paths.get(storageLocation, storagePath);
        return Files.deleteIfExists(path);
    }

    @Override
    public boolean isFileExists(String storagePath) {
        Path path = Paths.get(storageLocation, storagePath);
        return  Files.exists(path);
    }
}
