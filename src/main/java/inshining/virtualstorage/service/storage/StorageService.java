package inshining.virtualstorage.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    InputStream getFileAsInputStream(String storagePath) throws IOException;
    boolean uploadFile(String storagePath, MultipartFile file) throws IOException;
    boolean deleteFile(String storagePath) throws IOException;
    boolean isFileExists(String storagePath);
}