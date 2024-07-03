package inshining.virtualstorage.dto;

import org.springframework.http.MediaType;

import java.io.InputStream;

public record FileDownloadDTO(InputStream inputStream, String filename, MediaType contentType, long size){
}
