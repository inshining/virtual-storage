package inshining.virtualstorage.metadata.repository;

import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetadataJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class MetaDataRepositoryTest {

    @Autowired
    private MetadataJpaRepository metadataJpaRepository;

    private MetaData metaData;

    @BeforeEach
    void setMetaData(){
        metaData = new FileMetaData(UUID.randomUUID(), "test", "text/plain", "test.txt", 1000);
    }

    @DisplayName("파일 메타데이터 저장하기")
    @Test
    void createFileMetaDataTest(){
        MetaData saveMetaData = metadataJpaRepository.save(metaData);
        Assertions.assertThat(saveMetaData).isNotNull();
        Assertions.assertThat(saveMetaData.getId()).isEqualTo(metaData.getId());
        Assertions.assertThat(saveMetaData.getUsername()).isEqualTo(metaData.getUsername());
        Assertions.assertThat(saveMetaData.getContentType()).isEqualTo(metaData.getContentType());
        Assertions.assertThat(saveMetaData.getOriginalFilename()).isEqualTo(metaData.getOriginalFilename());
        Assertions.assertThat(saveMetaData.getSize()).isEqualTo(metaData.getSize());
    }

    @DisplayName("실패: 파일 메타데이터 저장하기")
    @Test
    void createFileMetaDataTest_Then_Fail(){
        assertThrows(InvalidDataAccessApiUsageException.class, () -> metadataJpaRepository.save(null));
    }

    @DisplayName("파일 메타데이터 삭제하기")
    @Test
    void deleteFileMetaDataTest(){
        MetaData saveMetaData = metadataJpaRepository.save(metaData);
        metadataJpaRepository.delete(saveMetaData);
        Assertions.assertThat(metadataJpaRepository.existsById(saveMetaData.getId())).isFalse();
    }

    @DisplayName("실패: 파일 메타데이터 삭제하기")
    @Test
    void deleteFileMetaDataTest_Then_Fail(){
        assertThrows(InvalidDataAccessApiUsageException.class, () -> metadataJpaRepository.delete(null));
    }

    @DisplayName("파일 메타데이터 조회하기")
    @Test
    void findFileMetaDataTest(){
        MetaData saveMetaData = metadataJpaRepository.save(metaData);
        MetaData findMetaData = metadataJpaRepository.findByOriginalFilenameAndUsername(saveMetaData.getOriginalFilename(), saveMetaData.getUsername());
        Assertions.assertThat(findMetaData).isNotNull();
        Assertions.assertThat(findMetaData.getId()).isEqualTo(metaData.getId());
        Assertions.assertThat(findMetaData.getUsername()).isEqualTo(metaData.getUsername());
        Assertions.assertThat(findMetaData.getContentType()).isEqualTo(metaData.getContentType());
        Assertions.assertThat(findMetaData.getOriginalFilename()).isEqualTo(metaData.getOriginalFilename());
        Assertions.assertThat(findMetaData.getSize()).isEqualTo(metaData.getSize());
    }

    @DisplayName("폴더 메타데이터 존재여부 확인하기")
    @Test
    void existsByOriginalFilenameAndUsernameInFoldersTest(){
        MetaData saveMetaData = metadataJpaRepository.save(metaData);
        boolean exists = metadataJpaRepository.existsByOriginalFilenameAndUsernameAndStorageType(saveMetaData.getOriginalFilename(), saveMetaData.getUsername(), "FOLDER");
        Assertions.assertThat(exists).isFalse();

        FolderMetaData folderMetaData = new FolderMetaData(UUID.randomUUID(), "test", "testFolder");
        MetaData saveFolderMetaData = metadataJpaRepository.save(folderMetaData);
        boolean existsFolder = metadataJpaRepository.existsByOriginalFilenameAndUsernameAndStorageType(saveFolderMetaData.getOriginalFilename(), saveFolderMetaData.getUsername(), "FOLDER");
        Assertions.assertThat(existsFolder).isTrue();
    }

    @DisplayName("폴더 속 메타데이터 조회하기")
    @Test
    void findAllByParentTest(){
        FolderMetaData folderMetaData = new FolderMetaData(UUID.randomUUID(), "test", "testFolder");
        metaData.setParent(folderMetaData);
        FileMetaData fileMetaData = new FileMetaData(UUID.randomUUID(), "test", "text/plain", "test2.txt", 1000, "testFolder/", folderMetaData);
        FileMetaData fileMetaData2 = new FileMetaData(UUID.randomUUID(), "test", "text/plain", "test3.txt", 1000, "testFolder/", folderMetaData);
        MetaData saveFolderMetaData = metadataJpaRepository.save(folderMetaData);
        MetaData saveMetaData = metadataJpaRepository.save(metaData);
        MetaData saveMetaData2 = metadataJpaRepository.save(fileMetaData);
        MetaData saveMetaData3 = metadataJpaRepository.save(fileMetaData2);

        List<MetaData> list = metadataJpaRepository.findAllByParent(saveFolderMetaData);

        Assertions.assertThat(list.size()).isEqualTo(3);
        Assertions.assertThat(list).contains(saveMetaData);
        Assertions.assertThat(list).contains(saveMetaData2);
        Assertions.assertThat(list).contains(saveMetaData3);
    }

    @DisplayName("경로와 사용자로 폴더 조회하기")
    @Test
    void findByPathAndUsernameAndStorageTypeTest(){
        String user = "test";
        String folderName = "superFolder";
        FolderMetaData folderMetaData = new FolderMetaData(UUID.randomUUID(), user, folderName);
        MetaData saveFolderMetaData = metadataJpaRepository.save(folderMetaData);
        FolderMetaData findFolderMetaData = metadataJpaRepository.findByPathAndUsernameAndStorageType(saveFolderMetaData.getPath(), saveFolderMetaData.getUsername(), "FOLDER");

        MetaData subFolder = new FolderMetaData(UUID.randomUUID(), user, "subFolder", "/testFolder/", findFolderMetaData);
        metadataJpaRepository.save(subFolder);
        FolderMetaData findSubFolderMetaData = metadataJpaRepository.findByPathAndUsernameAndStorageType(subFolder.getPath(), subFolder.getUsername(), "FOLDER");

        Assertions.assertThat(findFolderMetaData).isNotNull();
        Assertions.assertThat(findFolderMetaData.getId()).isEqualTo(folderMetaData.getId());
        Assertions.assertThat(findFolderMetaData.getUsername()).isEqualTo(folderMetaData.getUsername());
        Assertions.assertThat(findFolderMetaData.getOriginalFilename()).isEqualTo(folderMetaData.getOriginalFilename());
        Assertions.assertThat(findFolderMetaData.getPath()).isEqualTo(folderMetaData.getPath());

        Assertions.assertThat(findSubFolderMetaData).isNotNull();
        Assertions.assertThat(findSubFolderMetaData.getId()).isEqualTo(subFolder.getId());
        Assertions.assertThat(findSubFolderMetaData.getUsername()).isEqualTo(subFolder.getUsername());
        Assertions.assertThat(findSubFolderMetaData.getOriginalFilename()).isEqualTo(subFolder.getOriginalFilename());
        Assertions.assertThat(findSubFolderMetaData.getPath()).isEqualTo(subFolder.getPath());
    }
}
