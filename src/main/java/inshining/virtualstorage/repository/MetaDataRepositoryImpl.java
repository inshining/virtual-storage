package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class MetaDataRepositoryImpl implements MetaDataRepository {
    private static final String FOLDER = "FOLDER";

private final MetadataJpaRepository metadataJpaRepository;


    @Override
    public MetaData save(MetaData metaData) {
        if (metaData == null) throw new IllegalArgumentException("MetaData is null");
        return metadataJpaRepository.save(metaData);
    }

    @Override
    public Boolean existsById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        return metadataJpaRepository.existsById(id);
    }


    @Override
    public MetaData findByOriginalFilenameAndUsername(String filename, String username) {
        return metadataJpaRepository.findByOriginalFilenameAndUsername(filename, username);
    }

    @Override
    public void delete(MetaData metaData) {
        if (metaData == null) throw new IllegalArgumentException("MetaData is null");
        metadataJpaRepository.delete(metaData);
    }

    @Override
    public boolean existsByOriginalFilenameAndUsernameInFolders(String folderName, String user) {
        return metadataJpaRepository.existsByOriginalFilenameAndUsernameAndStorageType(folderName, user, FOLDER);
    }

    @Override
    public List<MetaData> findAllByParent(MetaData metaData) {
        return metadataJpaRepository.findAllByParent(metaData);
    }

    @Override
    public FolderMetaData findFolderByPathAndUsername(String path, String username) {
        return metadataJpaRepository.findByPathAndUsernameAndStorageType(path, username, FOLDER);
    }

    @Override
    public FolderMetaData findByUsernameAndPathInFolders(String username, String path) {
        return metadataJpaRepository.existsByUsernameAndPathAndStorageType(username, path, FOLDER);
    }

    @Override
    public boolean existsByUsernameAndPathAndOriginalFilenameInFolder(String username, String pathName, String folderName) {
        return metadataJpaRepository.existsByUsernameAndPathAndOriginalFileNameAndStorageType(username, pathName, folderName,FOLDER);
    }
}
