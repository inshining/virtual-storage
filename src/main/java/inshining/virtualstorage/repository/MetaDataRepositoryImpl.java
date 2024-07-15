package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.MetaData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
