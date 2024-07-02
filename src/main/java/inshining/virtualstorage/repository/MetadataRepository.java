package inshining.virtualstorage.repository;

import inshining.virtualstorage.model.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetaData, UUID> {

    MetaData findByOriginalFilenameAndUsername(String filename, String username);

}
