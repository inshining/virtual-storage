package inshining.virtualstorage.metadata.service;

import exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.repository.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.FolderMetaDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FolderMetaDataServiceTest {

    private FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();

    private FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);

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
        folderMetaDataService.createFolder("user", "folder1");

        // when
        Assertions.assertThrows(DuplicateFileNameException.class, () -> {
            folderMetaDataService.createFolder("user", "folder1");
        });
        // then
    }
}
