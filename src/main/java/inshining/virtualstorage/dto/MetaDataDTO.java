package inshining.virtualstorage.dto;

import java.time.LocalDateTime;

public record MetaDataDTO(String OwnerName, String OriginalFilename, String ContentType, long Size, LocalDateTime CreatedAt, LocalDateTime UpdatedAt) {
}
