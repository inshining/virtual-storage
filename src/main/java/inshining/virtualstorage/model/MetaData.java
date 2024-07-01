package inshining.virtualstorage.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class MetaData {

    @Id
    private UUID id;

    private String username;
    private String contentType;
    private String originalFilename;
    private long size;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MetaData() {}

    public MetaData(UUID id, String username, String contentType, String originalFilename, long size) {
        this.id = id;
        this.username = username;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
        this.size = size;
    }
}
