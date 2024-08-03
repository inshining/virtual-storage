package inshining.virtualstorage.service.storage;

public interface FolderStorageService {
    boolean createFolder(String username, String storagePath);
    boolean renameFolderName(String username, String folderName, String targetFolderName);

    boolean deleteFolder(String username, String folderName);
}
