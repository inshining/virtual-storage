package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.repository.FakeFolderMetaDataRepository;
import inshining.virtualstorage.service.FolderMetaDataService;
import inshining.virtualstorage.service.FolderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FolderServiceTest {
    private final FakeFolderMetaDataRepository folderMetaDataRepository = new FakeFolderMetaDataRepository();
    private final FolderMetaDataService folderMetaDataService = new FolderMetaDataService(folderMetaDataRepository);
    private final FolderService folderService = new FolderService(folderMetaDataService);



    @DisplayName("폴더 생성하기")
    @Test
    void createFolderTest() {
        // when
        FolderCreateResponse response = folderService.createFolder("user", "folder1");

        // then
        Assertions.assertEquals("user", response.ownerName());
        Assertions.assertEquals("folder1", response.folderName());
        Assertions.assertEquals("/", response.path());

        //when
        FolderCreateResponse response2 = folderService.createFolder("user", "folder1/folder2");

        // then
        Assertions.assertEquals("user", response2.ownerName());
        Assertions.assertEquals("folder2", response2.folderName());
        Assertions.assertEquals("folder1/", response2.path());
    }
}
