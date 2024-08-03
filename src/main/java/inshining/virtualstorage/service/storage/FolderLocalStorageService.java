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

    public boolean renameFolderName(String username, String folderName, String targetFolderName) {
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

    @Override
    public boolean deleteFolder(String username, String folderName) {
        Path path = Paths.get(STORAGE_LOCATION, username, folderName);

        // 폴더 존재하지 않을 때
        if (!Files.exists(path)) {
//            System.out.println("Folder does not exist: " + path.toString());
            return false;
        }
        try {
            deleteFolder(path);
//            System.out.println("Folder deleted successfully using Files: " + path.toString());
        } catch (IOException e) {
            e.printStackTrace();
//            System.out.println("Failed to delete folder using Files: " + path.toString());
            return false;
        }
        return true;
    }

    /**
     * 폴더 내 하위 파일 및 폴더 모두 삭제
     * @param path
     * @throws IOException
     */
    private void deleteFolder(Path path) throws IOException{
        // 디렉토리 하위 파일 개수 구하기
        long subFileCount = Files.list(path).count();

        // 하위 파일이 없으면 더이상 탐색하지 않고 삭제
        if (subFileCount == 0) {
            Files.delete(path);
            return;
        }

        // 하위 파일이 많으면 재귀적으로(DFS) 삭제
        if (subFileCount > 0) {
            for (Path subPath : Files.list(path).toList()) {
                if (Files.isDirectory(subPath)) {
                    deleteFolder(subPath);
                } else{
                    Files.delete(subPath);
                }
            }
        }
        Files.delete(path);
    }
}
