package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.exception.NoExistFolderException;
import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.repository.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.metadata.FileMetaDataService;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class FolderMetaDataServiceTest {

    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();

    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);

    private final FileMetaDataService fileMetaDataService = new FileMetaDataService(folderMetaDataRepository);

    private static String username = "username1";
    private static String originalName = "originName1";
    private static String changedName = "changedName";

    @DisplayName("폴더 생성")
    @Test
    void createFolderMetaDataTest() {
        // given
        // when
        FolderCreateResponse response = folderMetaDataService.createFolder("user", "folder1");
        // then

        Assertions.assertEquals("user", response.ownerName());
        Assertions.assertEquals("folder1", response.folderName());
        Assertions.assertEquals("/", response.path());


        FolderCreateResponse response2 = folderMetaDataService.createFolder("user2", "root/folder2");

        Assertions.assertEquals("user2", response2.ownerName());
        Assertions.assertEquals("folder2", response2.folderName());
        Assertions.assertEquals("root/", response2.path());

        FolderCreateResponse response3 = folderMetaDataService.createFolder("user3", "root/temp/folder3");

        Assertions.assertEquals("user3", response3.ownerName());
        Assertions.assertEquals("folder3", response3.folderName());
        Assertions.assertEquals("root/temp/", response3.path());
    }

    @DisplayName("이미 존재하는 폴더")
    @Test
    void alreadyExistFolderNotCreateTest() {
        // given
        // when
        folderMetaDataService.createFolder("user", "folder1");

        // then
        Assertions.assertThrows(DuplicateFileNameException.class, () -> folderMetaDataService.createFolder("user", "folder1"));
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
        Assertions.assertEquals(0, response.metaDataDTOS().size());

        // 파일 메타 데이터 생성
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file1",100, "folder1/", folderMetaData ));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file2",100, "folder1/", folderMetaData));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file3",100, "folder1/", folderMetaData));

        Assertions.assertEquals(3, folderMetaDataService.listMetadataInFolder("user", "folder1").metaDataDTOS().size());
    }

    @DisplayName("서로 다른 유저일 경우 폴더 내 파일 구분")
    @Test
    void listMetadataInFolderDifferentUserTest(){
        // given
        folderMetaDataService.createFolder("user", "folder1");
        FolderMetaData folderMetaData = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("folder1", "user");

        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file1",100, "folder1/", folderMetaData ));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file2",100, "folder1/", folderMetaData));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user",  "text/plain", "file3",100, "folder1/", folderMetaData));


        folderMetaDataService.createFolder("user2", "folder1");
        FolderMetaData folderMetaData2 = (FolderMetaData) folderMetaDataRepository.findByOriginalFilenameAndUsername("folder1", "user2");
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user2",  "text/plain", "file2",100, "folder1/", folderMetaData2));
        fileMetaDataService.save(new FileMetaData(UUID.randomUUID(), "user2",  "text/plain", "file3",100, "folder1/", folderMetaData2));
        // when
        var response = folderMetaDataService.listMetadataInFolder("user", "folder1");

        // then
        Assertions.assertEquals(3, response.metaDataDTOS().size());

        var response2 = folderMetaDataService.listMetadataInFolder("user2", "folder1");

        Assertions.assertEquals(2, response2.metaDataDTOS().size());
    }


    @DisplayName("폴더 명 변경")
    @Test
    void renameFolderNameTest(){
        folderMetaDataService.createFolder(username, originalName);

        FolderCreateResponse folderCreateResponse = folderMetaDataService.renameFolder(username, originalName, changedName);

        Assertions.assertEquals(username, folderCreateResponse.ownerName());
        Assertions.assertEquals(changedName, folderCreateResponse.folderName());

        String originName2 = "origin";
        String changedName2 = "dfafaffad";

        folderMetaDataService.createFolder(username, originName2);

        FolderCreateResponse folderCreateResponse2 = folderMetaDataService.renameFolder(username, originName2, changedName2);

        Assertions.assertEquals(username, folderCreateResponse2.ownerName());
        Assertions.assertEquals(changedName2, folderCreateResponse2.folderName());
    }

    @DisplayName("존재 하지 않은 폴더의 이름을 변경하고자 할때 실패")
    @Test
    void noExistFolderName_Rename_Then_Fail(){
        String noExistName = "noExistName";

        folderMetaDataService.createFolder(username, originalName);

        Assertions.assertThrows(NoExistFolderException.class, () -> folderMetaDataService.renameFolder(username, noExistName, changedName));
    }
    @DisplayName("존재 하지 않은 사용자의 폴더 이름을 변경하고자 할때 실패")
    @Test
    void noExistUserName_Rename_Then_Fail(){
        String noExistUserName = "noExistName";

        folderMetaDataService.createFolder(username, originalName);

        Assertions.assertThrows(NoExistFolderException.class, () -> folderMetaDataService.renameFolder(noExistUserName, originalName, changedName));
    }
}
