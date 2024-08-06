package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.exception.NoExistFolderException;
import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.util.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.metadata.FileMetaDataService;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FolderMetaDataServiceTest {

    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();

    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);

    private final FileMetaDataService fileMetaDataService = new FileMetaDataService(folderMetaDataRepository);

    private static final String username = "username1";
    private static final String originalName = "originName1";
    private static final String changedName = "changedName";

    @AfterEach
    void tearDown(){
        folderMetaDataRepository.clear();
    }

    @DisplayName("폴더 생성")
    @Test
    void createFolderMetaDataTest() {
        // given
        // when
        FolderCreateResponse response = folderMetaDataService.createFolder("user", "folder1");
        // then

        assertEquals("user", response.ownerName());
        assertEquals("folder1", response.folderName());
        assertEquals("/", response.path());


        FolderCreateResponse response2 = folderMetaDataService.createFolder("user2", "root/folder2");

        assertEquals("user2", response2.ownerName());
        assertEquals("folder2", response2.folderName());
        assertEquals("root/", response2.path());

        FolderCreateResponse response3 = folderMetaDataService.createFolder("user3", "root/temp/folder3");

        assertEquals("user3", response3.ownerName());
        assertEquals("folder3", response3.folderName());
        assertEquals("root/temp/", response3.path());
    }

    @DisplayName("이미 존재하는 폴더")
    @Test
    void alreadyExistFolderNotCreateTest() {
        // given
        // when
        folderMetaDataService.createFolder("user", "folder1");

        // then
        assertThrows(DuplicateFileNameException.class, () -> folderMetaDataService.createFolder("user", "folder1"));
    }

    @DisplayName("폴더 아래 여러 파일 보여주기")
    @Test
    void listMetadataInFolderTest(){
        // given
        folderMetaDataService.createFolder("user", "folder1");
        folderMetaDataService.createFolder("user", "folder2");
        folderMetaDataService.createFolder("user", "folder3");

        FolderMetaData folderMetaData = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("folder1", "user");
        // when
        var response = folderMetaDataService.listMetadataInFolder("user", "folder1");

        // then
        assertEquals(0, response.metaDataDTOS().size());

        // 파일 메타 데이터 생성
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file1",100, "folder1/", folderMetaData ));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file2",100, "folder1/", folderMetaData));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file3",100, "folder1/", folderMetaData));

        assertEquals(3, folderMetaDataService.listMetadataInFolder("user", "folder1").metaDataDTOS().size());
    }

    @DisplayName("서로 다른 유저일 경우 폴더 내 파일 구분")
    @Test
    void listMetadataInFolderDifferentUserTest(){
        // given
        folderMetaDataService.createFolder("user", "folder1");
        FolderMetaData folderMetaData = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("folder1", "user");

        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file1",100, "folder1/", folderMetaData ));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file2",100, "folder1/", folderMetaData));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file3",100, "folder1/", folderMetaData));


        folderMetaDataService.createFolder("user2", "folder1");
        FolderMetaData folderMetaData2 = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("folder1", "user2");
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user2",  "text/plain", "file2",100, "folder1/", folderMetaData2));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), "user2",  "text/plain", "file3",100, "folder1/", folderMetaData2));
        // when
        var response = folderMetaDataService.listMetadataInFolder("user", "folder1");

        // then
        assertEquals(3, response.metaDataDTOS().size());

        var response2 = folderMetaDataService.listMetadataInFolder("user2", "folder1");

        assertEquals(2, response2.metaDataDTOS().size());
    }


    @DisplayName("폴더 명 변경")
    @Test
    void renameFolderNameTest(){
        folderMetaDataService.createFolder(username, originalName);

        FolderCreateResponse folderCreateResponse = folderMetaDataService.renameFolder(username, originalName, changedName);

        assertEquals(username, folderCreateResponse.ownerName());
        assertEquals(changedName, folderCreateResponse.folderName());

        String originName2 = "origin";
        String changedName2 = "dfafaffad";

        folderMetaDataService.createFolder(username, originName2);

        FolderCreateResponse folderCreateResponse2 = folderMetaDataService.renameFolder(username, originName2, changedName2);

        assertEquals(username, folderCreateResponse2.ownerName());
        assertEquals(changedName2, folderCreateResponse2.folderName());
    }

    @DisplayName("존재 하지 않은 폴더의 이름을 변경하고자 할때 실패")
    @Test
    void noExistFolderName_Rename_Then_Fail(){
        String noExistName = "noExistName";

        folderMetaDataService.createFolder(username, originalName);

        assertThrows(NoExistFolderException.class, () -> folderMetaDataService.renameFolder(username, noExistName, changedName));
    }
    @DisplayName("존재 하지 않은 사용자의 폴더 이름을 변경하고자 할때 실패")
    @Test
    void noExistUserName_Rename_Then_Fail(){
        String noExistUserName = "noExistName";

        folderMetaDataService.createFolder(username, originalName);

        assertThrows(NoExistFolderException.class, () -> folderMetaDataService.renameFolder(noExistUserName, originalName, changedName));
    }

    @DisplayName("폴더 1개 삭제 성공")
    @Test
    void deleteFolderSuccess(){
        folderMetaDataService.createFolder(username, originalName);
        folderMetaDataService.deleteFolder(username, originalName);

        assertEquals(0, folderMetaDataRepository.findAll().size());
    }

    @DisplayName("상위 폴더 삭제시 하위 폴더 삭제")
    @Test
    void deleteSuperFolderThenDeleteSubFolder_Success(){
        //given
        folderMetaDataService.createFolder(username, "root");
        folderMetaDataService.createFolder(username, "/root/folder1");
        folderMetaDataService.createFolder(username, "/root/folder1/folder2");

        //when
        folderMetaDataService.deleteFolder(username, "root/folder1");

        //then
        assertEquals(1, folderMetaDataRepository.findAll().size());
        FolderMetaData root = folderMetaDataRepository.findFolderByPathAndUsername("/", username);
        assertEquals(0, folderMetaDataRepository.findAllByParent(root).size());
        assertNotNull(root);
    }

    @DisplayName("상위 폴더 삭제시 하위 폴더 삭제")
    @Test
    void deleteSuperFolderThenDeleteSubFiles_Success(){
        //given
        folderMetaDataService.createFolder(username, "root");
        folderMetaDataService.createFolder(username, "root/folder1");
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), username,  "text/plain", "file1",100, "root/folder1/", folderMetaDataRepository.findFolderByPathAndUsername("root/folder1/", username) ));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), username,  "text/plain", "file2",100, "root/folder1/", folderMetaDataRepository.findFolderByPathAndUsername("root/folder1/", username) ));

        //when
        folderMetaDataService.deleteFolder(username, "root/folder1");

        //then
        assertEquals(1, folderMetaDataRepository.findAll().size());
        FolderMetaData root = folderMetaDataRepository.findFolderByPathAndUsername("/", username);
        assertEquals(0, folderMetaDataRepository.findAllByParent(root).size());
        assertNotNull(root);
    }

    @DisplayName("루트 폴더 삭제시 손자 폴더 삭제 성공")
    @Test
    void deleteRootFolderThenDeleteGrandSonFolder_Success(){
        //given
        folderMetaDataService.createFolder(username, "/root");
        folderMetaDataService.createFolder(username, "/root/folder1");
        folderMetaDataService.createFolder(username, "/root/folder1/folder2");

        //when
        folderMetaDataService.deleteFolder(username, "root");

        //then
        assertEquals(0, folderMetaDataRepository.findAll().size());
    }

    @DisplayName("폴더 아닌 파일 삭제시 실패")
    @Test
    void deleteFileThenFail(){
        //given
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), username,  "text/plain", "file1",100, "root/", folderMetaDataRepository.findFolderByPathAndUsername("root/", username) ));

        //when
        boolean result = folderMetaDataService.deleteFolder(username, "root");

        //then
        assertFalse(result);
    }

    @DisplayName("존재하지 않는 폴더 삭제시 실패")
    @Test
    void deleteNoExistFolderThenFail(){
        //given
        folderMetaDataService.createFolder(username, originalName);

        //when
        boolean result = folderMetaDataService.deleteFolder(username, changedName);

        //then
        assertFalse(result);
    }

    @DisplayName("성공: 폴더 이동")
    @Test
    void moveFolderSuccess(){
        // given
        Path sourcePath = Paths.get(username, "testFolder1/testFolder2/testFolder3/");
        Path destPath = Paths.get(username, "testFolder4/testFolder5/");
        folderMetaDataService.createFolder(username, sourcePath.toString());
        folderMetaDataService.createFolder(username, destPath.toString());
        FolderMetaData source = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder3", username);
        FolderMetaData dest = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder5", username);

        //when
        assertTrue(folderMetaDataService.move(username, sourcePath, destPath));

        // then
        assertEquals(dest, source.getParent());
    }

    @DisplayName("성공: 폴더 하위 파일 모두 이동")
    @Test
    void moveSubFilesSuccess(){
        // given
        Path sourcePath = Paths.get(username, "testFolder1/testFolder2/testFolder3/");
        Path destPath = Paths.get(username, "testFolder4/testFolder5/");
        folderMetaDataService.createFolder(username, sourcePath.toString());
        folderMetaDataService.createFolder(username, destPath.toString());

        FolderMetaData source = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder3", username);
        FolderMetaData dest = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder5", username);

        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), username,  "text/plain", "file1",100, sourcePath.toString(), source ));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), username,  "text/plain", "file2",100, sourcePath.toString(), source ));
        fileMetaDataService.createFile(new FileMetaData(UUID.randomUUID(), username,  "text/plain", "file3",100, sourcePath.toString(), source ));

        //when
        assertTrue(folderMetaDataService.move(username, sourcePath, destPath));


        // then
        MetaData file1 = folderMetaDataRepository.findByOriginalFilenameAndUsername("file1", username);
        MetaData file2 = folderMetaDataRepository.findByOriginalFilenameAndUsername("file2", username);
        MetaData file3 = folderMetaDataRepository.findByOriginalFilenameAndUsername("file3", username);

        MetaData toBeFolder = folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder3", username);

        assertEquals(dest, source.getParent());
        assertEquals(3, folderMetaDataRepository.findAllByParent(source).size());

        Path destinedPath = Paths.get(dest.getPath(), dest.getOriginalFilename(), source.getOriginalFilename());
        Path sourcePath2 = Paths.get(toBeFolder.getPath(), toBeFolder.getOriginalFilename());

        assertEquals(destinedPath.toString(), sourcePath2.toString());
        assertEquals(destinedPath.toString(), file1.getPath());
        assertEquals(destinedPath.toString(), file1.getPath());
        assertEquals(destinedPath.toString(), file2.getPath());
        assertEquals(destinedPath.toString(), file3.getPath());
    }

    @DisplayName("성공: 3중 하위 폴더 전체 옮기기")
    @Test
    void moveSubFolderSuccess(){
        // given
        Path sourcePath = Paths.get(username, "testFolder1/testFolder2/testFolder3/");
        Path destPath = Paths.get(username, "testFolder4/testFolder5/");
        folderMetaDataService.createFolder(username, sourcePath.toString());
        folderMetaDataService.createFolder(username, destPath.toString());

        Path path6 = Paths.get(sourcePath.toString() , "testFolder6");
        Path path7 = Paths.get(path6.toString() , "testFolder7");
        folderMetaDataService.createFolder(username, path6.toString());
        folderMetaDataService.createFolder(username, path7.toString());

        //when
        assertTrue(folderMetaDataService.move(username, sourcePath, destPath));

        // then
        FolderMetaData toBeFolder = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder7", username);
        FolderMetaData toBeFolder2 = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("testFolder6", username);

        assertEquals(toBeFolder.getParent(), toBeFolder2);
        assertTrue(toBeFolder.getPath().startsWith(destPath.toString()));
        assertTrue(toBeFolder2.getPath().startsWith(destPath.toString()));
    }

    @DisplayName("실패: 폴더 이동 - 폴더가 존재하지 않는 경우")
    @Test
    void moveFolderFail(){
        // given
        Path sourcePath = Paths.get(username, "testFolder1/testFolder2/testFolder3/");
        Path destPath = Paths.get(username, "testFolder4/testFolder5/");
        folderMetaDataService.createFolder(username, sourcePath.toString());

        //when
        assertFalse(folderMetaDataService.move(username, sourcePath, destPath));
    }
}
