package inshining.virtualstorage;

import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import inshining.virtualstorage.util.FileDeletor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FolderIntegrationTest {
    private String LOCAL_STORAGE_PATH = "upload/";
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Test
    void testCreateFolder() {
        String username = "testUser";
        String folderName = "testFolder";
        FolderCreateRequest request = new FolderCreateRequest(username, folderName);

        ResponseEntity response = restTemplate.postForEntity("/folder/", request, String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(metaDataRepository.findByOriginalFilenameAndUsername(folderName, username) != null);
        MetaData metaData =metaDataRepository.findByOriginalFilenameAndUsername(folderName, username);
        Assertions.assertEquals(username, metaData.getUsername());
        Assertions.assertEquals(folderName, metaData.getOriginalFilename());

        Path path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);
        FileDeletor.delete(path, 2);

    }

}

