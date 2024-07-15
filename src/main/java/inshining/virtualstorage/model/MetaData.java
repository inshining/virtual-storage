package inshining.virtualstorage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "storage_type", discriminatorType = DiscriminatorType.STRING)
public abstract class MetaData {

    @Id
    protected UUID id;
    protected String username;
    protected String contentType;
    protected String originalFilename;
    protected long size;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    public MetaData() {}

    public MetaData(UUID id, String username, String contentType, String originalFilename, long size) {
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
