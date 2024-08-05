package inshining.virtualstorage.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface StorageService {
    InputStream getFileAsInputStream(String storagePath) throws IOException;
    boolean uploadFile(String storagePath, MultipartFile file) throws IOException;
    boolean deleteFile(String storagePath) throws IOException;
    boolean isFileExists(String storagePath);

    boolean createFolder(String username, String storagePath);
    boolean renameFolderName(String username, String folderName, String targetFolderName);

    boolean deleteFolder(String username, String folderName);

    boolean move(String username, Path source, Path destination);
}
