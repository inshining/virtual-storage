package inshining.virtualstorage.service;

import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@RequiredArgsConstructor
@Service
public class FileMetaDataService {

    private final MetaDataRepository metadataRepository;

    public MetaData save(FileMetaData fileMetaData){
        return metadataRepository.save(fileMetaData);
    }


    public MetaData findByOriginalFilenameAndUsername(String filename, String username) {
        return metadataRepository.findByOriginalFilenameAndUsername(filename, username);
    }

    public void delete(MetaData metaData) {
        metadataRepository.delete(metaData);
    }

    public boolean existsById(UUID uuid) {
        return metadataRepository.existsById(uuid);
    }
}
