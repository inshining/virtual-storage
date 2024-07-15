package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.MetaData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class MetaDataRepositoryImpl implements MetaDataRepository {

private final MetadataJpaRepository metadataJpaRepository;

    @Override
    public MetaData save(MetaData metaData) {
        return metadataJpaRepository.save(metaData);
    }

    @Override
    public Boolean existsById(UUID id) {
        return metadataJpaRepository.existsById(id);
    }

    @Override
    public MetaData findByOriginalFilenameAndUsername(String filename, String username) {
        return metadataJpaRepository.findByOriginalFilenameAndUsername(filename, username);
    }

    @Override
    public void delete(MetaData metaData) {
        metadataJpaRepository.delete(metaData);
    }

    @Override
    public boolean existsByOriginalFilenameAndUsernameInFolders(String folderName, String user) {
        return metadataJpaRepository.existsByOriginalFilenameAndUsernameAndStorageType(folderName, user, "FOLDER");
    }

    @Override
    public List<MetaData> findAllByParent(MetaData metaData) {
        return null;
    }
}
