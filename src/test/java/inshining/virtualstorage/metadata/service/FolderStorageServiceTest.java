package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.service.FolderLocalStorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FolderStorageServiceTest {
    private static final String storageLocation = "upload/";
    private FolderLocalStorageService folderLocalStorageService = new FolderLocalStorageService(storageLocation);


    @DisplayName("폴더 1개 만들기")
    @Test
    void createFolderTest(){
        Assertions.assertTrue(folderLocalStorageService.createFolder("testFolder"));

        Path path = Paths.get(storageLocation, "testFolder");
        Assertions.assertTrue(Files.exists(path));
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @DisplayName("존재하지 않은 하위 폴더 모두 만들기")
    @Test
    void createSubFoldersTest(){
        Assertions.assertTrue(folderLocalStorageService.createFolder("testFolder1/testFolder2/testFolder3"));

        Path path = Paths.get(storageLocation, "testFolder1/testFolder2/testFolder3");
        Assertions.assertTrue(Files.exists(path));

        if (Files.exists(path)) {
            try {
                Files.delete(path);
                Files.delete(path.getParent());
                Files.delete(path.getParent().getParent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @DisplayName("이미 존재하는 폴더 만들기")
    @Test
    void createExistingFolderTest(){
        Assertions.assertTrue(folderLocalStorageService.createFolder("testFolder"));

        Path path = Paths.get(storageLocation, "testFolder");
        Assertions.assertTrue(Files.exists(path));

        Assertions.assertFalse(folderLocalStorageService.createFolder("testFolder"));

        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Assertions.assertTrue(folderLocalStorageService.createFolder("testFolder/testFolder2"));

        Path path2 = Paths.get(storageLocation, "testFolder/testFolder2");
        Assertions.assertTrue(Files.exists(path2));

        Assertions.assertFalse(folderLocalStorageService.createFolder("testFolder"));

        if (Files.exists(path2)) {
            try {
                Files.delete(path2);
                Files.delete(path2.getParent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
