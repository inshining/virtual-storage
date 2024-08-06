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
    private String STORAGE_LOCATION = "upload/";

    @Override
    public InputStream getFileAsInputStream(String storagePath) throws IOException {
        Path path = Paths.get(STORAGE_LOCATION, storagePath);
        return Files.newInputStream(path);
    }

    @Override
    public boolean uploadFile(String storagePath, MultipartFile file) throws IOException {
        Path path = Paths.get(STORAGE_LOCATION, storagePath);
        if (!Files.exists(path.getParent())){
            Files.createDirectories(path.getParent());
        }
        Files.write(path, file.getBytes());
        return Files.exists(path);
    }

    @Override
    public boolean deleteFile(String storagePath) throws IOException {
        Path path = Paths.get(STORAGE_LOCATION, storagePath);
        return Files.deleteIfExists(path);
    }

    @Override
    public boolean isFileExists(String storagePath) {
        Path path = Paths.get(STORAGE_LOCATION, storagePath);
        return  Files.exists(path);
    }

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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean renameFolderName(String username, String folderName, String targetFolderName) {
        Path path = Paths.get(STORAGE_LOCATION, username, folderName);
        Path targetPath = Paths.get(STORAGE_LOCATION, username, targetFolderName);
        try {
            Files.move(path, targetPath);
//            System.out.println("Folder renamed successfully using Files: " + path.toString() + " -> " + targetPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
//            System.out.println("Failed to rename folder using Files: " + path.toString() + " -> " + targetPath.toString());
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
     * 파일 이동
     * @param username 이용자 이름을 넣어야 한다.
     * @param source - 이동하게 할 파일(디렉토리) 경로 (파일명, 폴더명 포함)
     * @param destinationPath - 이동할 경로 (파일명 포함 x)
     * @return 성공여부

    만약 파일이 존재하지 않으면 false를 반환하고, 파일이 존재하면 파일을 이동하고 true를 반환한다.
    파일과 폴더 관계없이 이동시키는 메소드이다. unix의 mv와 같은 기능을 한다.
     */
    @Override
    public boolean move(String username, Path source, Path destinationPath) {
        if (!Files.exists(source)) {
            return false;
        }

        Path destination = Paths.get(destinationPath.toString(), source.getFileName().toString());
        try {
            if (!Files.exists(destination)){
                Files.createDirectories(destination.getParent());
            }
            Files.move(source, destination);
//            System.out.println("File moved successfully using Files: " + source.toString() + " -> " + destination.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
//            System.out.println("Failed to move file using Files: " + source.toString() + " -> " + destination.toString());
        }
        return false;
    }

    /**
     * 폴더 내 하위 파일 및 폴더 모두 삭제
     * @param path 초기에 삭제 경로의 가장 최상위 경로를 입력한다.
     * @throws IOException 파일 삭제하는 과정에서 발생하는 에러
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
        for (Path subPath : Files.list(path).toList()) {
            if (Files.isDirectory(subPath)) {
                deleteFolder(subPath);
            } else{
                Files.delete(subPath);
            }
        }
        Files.delete(path);
    }
}
