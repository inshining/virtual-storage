package inshining.virtualstorage.dto;

import java.util.List;

public record FolderMetaResponse(String ownerName, String folderName, String path, List<MetaDataDTO> metaDataDTOS) {
}
