package inshining.virtualstorage.model;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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

    public FileMetaData(UUID id, String username, String contentType, String originalFilename, long size, String path, FolderMetaData parent) {
        this.id = id;
        this.username = username;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
        this.size = size;
        this.path = path;
        this.parent = parent;
    }

    public FileMetaData(MultipartFile file, String username){
        this.id = UUID.randomUUID();
        this.username = username;
        this.contentType = file.getContentType();
        this.originalFilename = file.getOriginalFilename();
        this.size = file.getSize();
    }

    public String getStoragePath() {
        return this.id.toString() + "-" + this.originalFilename;
    }

}
