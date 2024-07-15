package inshining.virtualstorage.model;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@DiscriminatorValue("FILE")
public class FileMetaData extends MetaData {

    public FileMetaData() {}

    public FileMetaData(UUID id, String username, String contentType, String originalFilename, long size) {
        this.id = id;
        this.username = username;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
        this.size = size;
    }

    public String getStoragePath() {
        return this.id.toString() + "-" + this.originalFilename;
    }
}
