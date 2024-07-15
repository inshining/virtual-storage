package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.MetaData;

import java.util.UUID;

public interface MetaDataRepository {
    MetaData save(MetaData metaData);

    Boolean existsById(UUID id);

    MetaData findByOriginalFilenameAndUsername(String filename, String username);

    void delete(MetaData fileMetaData);

    boolean existsByOriginalFilenameAndUsernameInFolders(String folderName, String user);
}
