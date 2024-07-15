package inshining.virtualstorage.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("FOLDER")
public class FolderMetaData extends MetaData{
        public static final String CONTENT_TYPE = "inode/directory";
        public FolderMetaData() {}

        public FolderMetaData(String username, String originalFilename) {
            this.username = username;
            this.originalFilename = originalFilename;
            this.contentType = CONTENT_TYPE;
        }

        public String getStoragePath() {
            return this.id.toString() + "-" + this.originalFilename;
        }
}
