package inshining.virtualstorage.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FolderLocalStorageService implements FolderStorageService {

    @Value("${storage.location}")
    private String STORAGE_LOCATION = "upload/";

    @Override
    public boolean createFolder(String username, String storagePath) {
        Path path = Paths.get(STORAGE_LOCATION, username,storagePath);
        String folderPath = path.toString();
        try {
            if (Files.exists(path)) {
                System.out.println("Folder already exists: " + folderPath);
                return false;
            }
            Files.createDirectories(path);
            System.out.println("Folder created successfully using Files: " + folderPath);
        } catch (IOException e) {
            e.printStackTrace();
            if (Files.exists(path)) {
                System.out.println("Folder already exists: " + folderPath);
            } else {
                System.out.println("Failed to create folder using Files: " + folderPath);
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public boolean changeFolderName(String username, String folderName, String targetFolderName) {
        Path path = Paths.get(STORAGE_LOCATION, username, folderName);
        Path targetPath = Paths.get(STORAGE_LOCATION, username, targetFolderName);
        try {
            Files.move(path, targetPath);
            System.out.println("Folder renamed successfully using Files: " + path.toString() + " -> " + targetPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to rename folder using Files: " + path.toString() + " -> " + targetPath.toString());
            return false;
        }
        return true;
    }
}
