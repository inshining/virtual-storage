package inshining.virtualstorage.model;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MetaData {

    private UUID id;

    private String username;
    private String contentType;
    private String originalFilename;
    private long size;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MetaData() {}

    public MetaData(UUID id, String username, String contentType, String originalFilename, long size) {
        this.id = id;
        this.username = username;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
        this.size = size;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
    }
}
