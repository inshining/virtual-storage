package inshining.virtualstorage.service;

import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.dto.FolderMetaResponse;
import inshining.virtualstorage.exception.NoExistFolderException;
import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.util.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.metadata.FileMetaDataService;
import inshining.virtualstorage.service.storage.LocalStorageService;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import inshining.virtualstorage.util.FileDeletor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

public class FolderServiceTest {
    private static final String LOCAL_STORAGE_PATH = "upload/";
    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();
    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);
    private final LocalStorageService localStorageService = new LocalStorageService();
    private final FolderService folderService = new FolderService(folderMetaDataService, localStorageService);

    private final FileMetaDataService fileMetaDataService = new FileMetaDataService(folderMetaDataRepository);
    
    private static  final String USERNAME = "user";
    private static  final String FOLDER_NAME = "folder1";

    @AfterEach
    void tearDown(){
        Path path = Paths.get(LOCAL_STORAGE_PATH);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }    }

    @DisplayName("폴더 생성하기")
    @Test
    void createFolderTest() {
        // when
        FolderCreateResponse response = folderService.createFolder(USERNAME, FOLDER_NAME);
        Path path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME);

        // then
        Assertions.assertEquals("user", response.ownerName());
        Assertions.assertEquals("folder1", response.folderName());
        Assertions.assertEquals("/", response.path());

        Assertions.assertTrue(Files.exists(path));
        FileDeletor.delete(path, 2);

        //when
        String FOLDER_NAME2 = "folder1/folder2";
        FolderCreateResponse response2 = folderService.createFolder("user", FOLDER_NAME2);
        path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME2);

        // then
        Assertions.assertEquals("user", response2.ownerName());
        Assertions.assertEquals("folder2", response2.folderName());
        Assertions.assertEquals("folder1/", response2.path());
        Assertions.assertTrue(Files.exists(path));
    }

    @DisplayName("동일 폴더 중복 생성시 에러 발생")
    @Test
    void duplicatedFolder_Then_Fail(){
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);

        // when
        Assertions.assertThrows(DuplicateFileNameException.class, () -> folderService.createFolder(USERNAME, FOLDER_NAME));

        // then
        Path path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME);
        Assertions.assertTrue(Files.exists(path));
    }

    @DisplayName("폴더 하위 파일들 리스트 가져오기")
    @Test
    void getFilesInFolderTest() {
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);

        // when
        FolderMetaResponse response = folderService.getMetaDataInFolder(USERNAME, FOLDER_NAME);

        // then
        Assertions.assertEquals(0, response.metaDataDTOS().size());
        Assertions.assertEquals(FOLDER_NAME, response.folderName());
        Assertions.assertEquals(USERNAME, response.ownerName());
        Assertions.assertEquals("/", response.path());

        FolderMetaData folderMetaData = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername(FOLDER_NAME, USERNAME);

        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file1",100, "folder1/", folderMetaData ));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file2",100, "folder1/", folderMetaData));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file3",100, "folder1/", folderMetaData));

        response = folderService.getMetaDataInFolder(USERNAME, FOLDER_NAME);
        Assertions.assertEquals(3, response.metaDataDTOS().size());
    }
    
    @DisplayName("폴더 이름변경하기")
    @Test
    void renameFolderTest() {
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);
        Path path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME);

        // when
        String newFolderName = "newFolderName";
        FolderCreateResponse response = folderService.renameFolder(USERNAME, FOLDER_NAME, newFolderName);
        Path newPath = Paths.get(LOCAL_STORAGE_PATH, USERNAME, newFolderName);

        // then

        Assertions.assertTrue(Files.exists(newPath));
        Assertions.assertFalse(Files.exists(path));
        Assertions.assertEquals(USERNAME, response.ownerName());
        Assertions.assertEquals(newFolderName, response.folderName());
    }

    @DisplayName("존재하지 않는 폴더 이름 변경할 경우 실패")
    @Test
    void noExistFolder_rename_Then_Fail() {
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);

        String noExistFolder = "noExistFolder";

        // when
        String newFolderName = "newFolderName";
        Assertions.assertThrows(NoExistFolderException.class, () -> folderService.renameFolder(USERNAME, noExistFolder, newFolderName));
    }

    @DisplayName("폴더 삭제하기")
    @Test
    void deleteFolderTest() {
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);
        Path path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME);

        // when
        boolean result = folderService.deleteFolder(USERNAME, FOLDER_NAME);

        // then
        Assertions.assertTrue(result);
        Assertions.assertFalse(Files.exists(path));
    }

    @DisplayName("존재하지 않는 폴더 삭제할 경우 실패")
    @Test
    void noExistFolder_delete_Then_Fail() {
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);

        String noExistFolder = "noExistFolder";

        // when
        Assertions.assertFalse(folderService.deleteFolder(USERNAME, noExistFolder));
    }

    @DisplayName("성공 폴더 이동하기")
    @Test
    void moveFolderTest() {
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);
        Path path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME);

        String destFolderName = "destFolder";
        folderService.createFolder(USERNAME, destFolderName);
        Path destPath = Paths.get(LOCAL_STORAGE_PATH, USERNAME, destFolderName);

        // when
        boolean result = folderService.move(USERNAME, FOLDER_NAME, destFolderName);

        // then
        Assertions.assertTrue(result);
        Assertions.assertFalse(Files.exists(path));
        Assertions.assertTrue(Files.exists(destPath));
    }

    @DisplayName("성공: 하위 여러 폴더 이동하기")
    @Test
    void moveSubFoldersTest(){
        // given
        folderService.createFolder(USERNAME, FOLDER_NAME);
        Path path = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME);

        String destFolderName = "destFolder";
        folderService.createFolder(USERNAME, destFolderName);
        Path destPath = Paths.get(LOCAL_STORAGE_PATH, USERNAME, destFolderName);

        String subFolderName = "subFolder";
        folderService.createFolder(USERNAME, FOLDER_NAME + "/" + subFolderName);
        Path subPath = Paths.get(LOCAL_STORAGE_PATH, USERNAME, FOLDER_NAME, subFolderName);

        // when
        boolean result = folderService.move(USERNAME, FOLDER_NAME, destFolderName);

        // then
        Assertions.assertTrue(result);
        Assertions.assertFalse(Files.exists(path));
        Assertions.assertTrue(Files.exists(destPath));

        Path expectedDestPath = Paths.get(LOCAL_STORAGE_PATH, USERNAME, destFolderName, FOLDER_NAME, subFolderName);
        Assertions.assertTrue(Files.exists(expectedDestPath));
    }

}
