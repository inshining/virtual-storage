package inshining.virtualstorage.controller;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.MetaDataFileResponse;
import inshining.virtualstorage.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/file")
public class MetaDataController {
    private final MetaDataService metaDataService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("user") String username) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        MetaDataFileResponse response =  metaDataService.uploadFile(file, username);
        if (response.isSuccess()){
            return ResponseEntity.ok(response.message());
        } else {
            return ResponseEntity.badRequest().body(response.message());
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<String> delete(@RequestParam("file") String filename, @RequestParam("user") String username) {
        MetaDataFileResponse response = metaDataService.deleteFile(filename, username);
        if (response.isSuccess()){
            return ResponseEntity.ok(response.message());
        } else {
            return ResponseEntity.badRequest().body(response.message());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("filename") String filename, @RequestParam("user") String username) {
        if (filename.isEmpty() || username.isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        FileDownloadDTO fileDownload = null;
        try{
            fileDownload = metaDataService.downloadFile(filename, username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
        InputStreamResource resource = new InputStreamResource(fileDownload.inputStream());
        return ResponseEntity.ok()
                .contentType(fileDownload.contentType())
                .contentLength(fileDownload.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileDownload.filename())
                .body(resource);
    }
}
