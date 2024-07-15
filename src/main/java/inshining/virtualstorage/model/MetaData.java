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
    protected String path = "/";

    @ManyToOne
    @JoinColumn(name = "parent_id")
    protected FolderMetaData parent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    public MetaData() {}

    public String getStoragePath() {
        return this.id.toString() + "-" + this.originalFilename;
    }
}
