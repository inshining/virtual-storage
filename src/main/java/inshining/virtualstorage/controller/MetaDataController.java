package inshining.virtualstorage.controller;

import inshining.virtualstorage.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/metadata")
public class MetaDataController {
    private final MetaDataService metaDataService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("user") String username) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        String body =  metaDataService.uploadFile(file, username);
        return ResponseEntity.ok(body);
    }
}
