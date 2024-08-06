package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface MetadataJpaRepository extends JpaRepository<MetaData, UUID> {


    MetaData findByOriginalFilenameAndUsername(String filename, String username);

    boolean existsByOriginalFilenameAndUsernameAndStorageType(String folderName, String user, String storage_type);

    List<MetaData> findAllByParent(MetaData saveMetaData);

    FolderMetaData findByPathAndUsernameAndStorageType(String path, String username, String folder);

    FolderMetaData existsByUsernameAndPathAndStorageType(String username, String path, String storage_type);

    boolean existsByUsernameAndPathAndOriginalFilenameAndStorageType(String username, String pathName, String folderName, String folder);
}
