package inshining.virtualstorage.service.storage;

import java.nio.file.Path;

public interface FolderStorageService {
    boolean createFolder(String username, String storagePath);
    boolean renameFolderName(String username, String folderName, String targetFolderName);

    boolean deleteFolder(String username, String folderName);

    boolean move(String username, Path source, Path destination);
}
