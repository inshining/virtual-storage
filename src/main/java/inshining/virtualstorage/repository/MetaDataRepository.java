package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;

import java.util.List;
import java.util.UUID;

public interface MetaDataRepository {
    MetaData save(MetaData metaData);

    Boolean existsById(UUID id);

    MetaData findByOriginalFilenameAndUsername(String filename, String username);

    void delete(MetaData fileMetaData);

    boolean existsByOriginalFilenameAndUsernameInFolders(String folderName, String user);

    List<MetaData> findAllByParent(MetaData metaData);

    FolderMetaData findFolderByPathAndUsername(String path, String username);

    FolderMetaData findByUsernameAndPathInFolders(String username, String path);

    boolean existsByUsernameAndPathAndOriginalFilenameInFolder(String username, String pathName, String folderName);
}
