package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.service.storage.FolderLocalStorageService;
import inshining.virtualstorage.util.FileDeletor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@EnabledOnOs({OS.MAC, OS.LINUX})
public class FolderStorageServiceTest {
    private static final String storageLocation = "upload/";
    private static final FolderLocalStorageService folderLocalStorageService = new FolderLocalStorageService();

    private static final String USERNAME = "testUser";


    @DisplayName("폴더 1개 만들기")
    @Test
    void createFolderTest(){
        Assertions.assertTrue(folderLocalStorageService.createFolder(USERNAME, "testFolder"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder");
        Assertions.assertTrue(Files.exists(path));
        FileDeletor.delete(path, 2);
    }

    @DisplayName("존재하지 않은 하위 폴더 모두 만들기")
    @Test
    void createSubFoldersTest(){
        Assertions.assertTrue(folderLocalStorageService.createFolder(USERNAME, "testFolder1/testFolder2/testFolder3"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder1/testFolder2/testFolder3");
        Assertions.assertTrue(Files.exists(path));

        FileDeletor.delete(path, 4);
    }

    @DisplayName("이미 존재하는 폴더 만들기")
    @Test
    void createExistingFolderTest(){
        Assertions.assertTrue(folderLocalStorageService.createFolder(USERNAME, "testFolder"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder");
        Assertions.assertTrue(Files.exists(path));

        Assertions.assertFalse(folderLocalStorageService.createFolder(USERNAME,"testFolder"));

        FileDeletor.delete(path, 2);

        Assertions.assertTrue(folderLocalStorageService.createFolder(USERNAME,"testFolder/testFolder2"));

        Path path2 = Paths.get(storageLocation, USERNAME,"testFolder/testFolder2");
        Assertions.assertTrue(Files.exists(path2));

        Assertions.assertFalse(folderLocalStorageService.createFolder(USERNAME,"testFolder"));

        FileDeletor.delete(path2, 3);
    }
}
