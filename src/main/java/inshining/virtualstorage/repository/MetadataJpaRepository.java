package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface MetadataJpaRepository extends JpaRepository<MetaData, UUID> {


    MetaData findByOriginalFilenameAndUsername(String filename, String username);

    boolean existsByOriginalFilenameAndUsernameAndStorageType(String folderName, String user, String storage_type);

    List<MetaData> findAllByParent(MetaData saveMetaData);
}
