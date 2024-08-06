package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.service.metadata.FileMetaDataService;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import inshining.virtualstorage.util.FakeFolderMetaDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FileMetaDataServiceTest {
    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();

    private final FileMetaDataService fileMetaDataService = new FileMetaDataService(folderMetaDataRepository);
    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);

    @DisplayName("성공: 파일 이동")
    @Test
    void moveFile() {
        // given
        String username = "root";
        String sourceFilename = "file1";
        String destinationPath = "/";
        FileMetaData fileMetaData = new FileMetaData(UUID.randomUUID(), username, "text/plain", sourceFilename, 8, "/", null);
        fileMetaDataService.createFile(fileMetaData);

        // when
        fileMetaDataService.moveFile(username, sourceFilename, Paths.get(destinationPath));

        // then
        MetaData finedMetaData = folderMetaDataRepository.findByOriginalFilenameAndUsername(sourceFilename, username);
        assertNotNull(finedMetaData);
        assertEquals(destinationPath, finedMetaData.getPath());
    }

    @DisplayName("성공: 폴더가 이미 존재할 경우 파일 이동")
    @Test
    void moveFileWithExistingFolder() {
        // given
        String username = "root";
        String sourceFilename = "file1";
        String sourcePath = "/";
        String destinationPath = "/folder1";
        FileMetaData fileMetaData = new FileMetaData(UUID.randomUUID(), username, "text/plain", sourceFilename, 8, sourcePath, null);
        fileMetaDataService.createFile(fileMetaData);
        folderMetaDataService.createFolder(username, destinationPath);

        // when
        fileMetaDataService.moveFile(username, sourceFilename, Paths.get(destinationPath));

        // then
        MetaData finedMetaData = folderMetaDataRepository.findByOriginalFilenameAndUsername(sourceFilename, username);
        assertNotNull(finedMetaData);
        assertEquals(destinationPath, finedMetaData.getPath());

        assertTrue(folderMetaDataRepository.existsByUsernameAndPathAndOriginalFilenameInFolder(username, sourcePath, "folder1"));
    }

    @DisplayName("실패: 파일이 존재하지 않을 경우 파일 이동")
    @Test
    void moveFileWithNotExistingFile() {
        // given
        String username = "root";
        String sourceFilename = "file1";
        String destinationPath = "/";
        FileMetaData fileMetaData = new FileMetaData(UUID.randomUUID(), username, "text/plain", sourceFilename, 8, "/", null);

        // when
        boolean result = fileMetaDataService.moveFile(username, sourceFilename, Paths.get(destinationPath));
        assertFalse(result);
    }
}
