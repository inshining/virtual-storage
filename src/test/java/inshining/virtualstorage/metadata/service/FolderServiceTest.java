package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.repository.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.FolderLocalStorageService;
import inshining.virtualstorage.service.FolderMetaDataService;
import inshining.virtualstorage.service.FolderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FolderServiceTest {
    private static final String LOCAL_STORAGE_PATH = "upload/";
    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();
    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);
    private final FolderLocalStorageService folderLocalStorageService = new FolderLocalStorageService(LOCAL_STORAGE_PATH);
    private final FolderService folderService = new FolderService(folderMetaDataService, folderLocalStorageService);



    @DisplayName("폴더 생성하기")
    @Test
    void createFolderTest() {
        // when
        String username = "user";
        String folderName = "folder1";
        FolderCreateResponse response = folderService.createFolder(username, folderName);
        Path path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);

        // then
        Assertions.assertEquals("user", response.ownerName());
        Assertions.assertEquals("folder1", response.folderName());
        Assertions.assertEquals("/", response.path());

        Assertions.assertTrue(Files.exists(path));
        if (Files.exists(path)) {
            try {
                Files.delete(path);
                Files.delete(path.getParent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //when
        folderName = "folder1/folder2";
        FolderCreateResponse response2 = folderService.createFolder("user", folderName);
        path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);

        // then
        Assertions.assertEquals("user", response2.ownerName());
        Assertions.assertEquals("folder2", response2.folderName());
        Assertions.assertEquals("folder1/", response2.path());
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
}
