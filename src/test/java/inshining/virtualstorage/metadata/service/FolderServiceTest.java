package inshining.virtualstorage.metadata.service;

import exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.dto.FolderMetaResponse;
import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.repository.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.metadata.FileMetaDataService;
import inshining.virtualstorage.service.storage.FolderLocalStorageService;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import inshining.virtualstorage.service.FolderService;
import inshining.virtualstorage.util.FileDeletor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FolderServiceTest {
    private static final String LOCAL_STORAGE_PATH = "upload/";
    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();
    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);
    private final FolderLocalStorageService folderLocalStorageService = new FolderLocalStorageService(LOCAL_STORAGE_PATH);
    private final FolderService folderService = new FolderService(folderMetaDataService, folderLocalStorageService);

    private final FileMetaDataService fileMetaDataService = new FileMetaDataService(folderMetaDataRepository);


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
        FileDeletor.delete(path, 2);

        //when
        folderName = "folder1/folder2";
        FolderCreateResponse response2 = folderService.createFolder("user", folderName);
        path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);

        // then
        Assertions.assertEquals("user", response2.ownerName());
        Assertions.assertEquals("folder2", response2.folderName());
        Assertions.assertEquals("folder1/", response2.path());
        Assertions.assertTrue(Files.exists(path));
        FileDeletor.delete(path, 3);
    }

    @DisplayName("동일 폴더 중복 생성시 에러 발생")
    @Test
    void duplicatedFolder_Then_Fail(){
        // given
        String username = "user";
        String folderName = "folder1";
        folderService.createFolder(username, folderName);

        // when
        Assertions.assertThrows(DuplicateFileNameException.class, () -> folderService.createFolder(username, folderName));

        // then
        Path path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);
        Assertions.assertTrue(Files.exists(path));
        FileDeletor.delete(path, 2);
    }

    @DisplayName("폴더 하위 파일들 리스트 가져오기")
    @Test
    void getFilesInFolderTest() {
        // given
        String username = "user";
        String folderName = "folder1";
        folderService.createFolder(username, folderName);
        Path path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);

        // when
        FolderMetaResponse response = folderService.getMetaDataInFolder(username, folderName);

        // then
        Assertions.assertEquals(0, response.metaDataDTOS().size());
        Assertions.assertEquals(folderName, response.folderName());
        Assertions.assertEquals(username, response.ownerName());
        Assertions.assertEquals("/", response.path());

        FolderMetaData folderMetaData = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername(folderName, username);

        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file1",100, "folder1/", folderMetaData ));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file2",100, "folder1/", folderMetaData));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file3",100, "folder1/", folderMetaData));

        response = folderService.getMetaDataInFolder(username, folderName);
        Assertions.assertEquals(3, response.metaDataDTOS().size());
        FileDeletor.delete(path, 2);

    }
}
