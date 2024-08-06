package inshining.virtualstorage;

import inshining.virtualstorage.dto.FolderRequestBody;
import inshining.virtualstorage.dto.MoveRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileIntegrationTest {
    static final String  LOCAL_STORAGE_PATH = "upload/";
    static final Path path = Paths.get(LOCAL_STORAGE_PATH);
    static final String user = "testuser";
    static final String testFileName = "test-file.txt";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown(){
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }    }

    @Test
    public void testFileUpload() {
        ResponseEntity<String> response = uploadSingleFile(getMultiValueMapHttpEntity());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testFileUploadFailure() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new byte[0]);

        ResponseEntity<String> response = uploadSingleFile(new HttpEntity<>(body, headers));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("성공: 파일 이동")
    @Test
    void moveSingleFile() {
        //given
        String destFolder = "testFolder";

        restTemplate.postForEntity("/folder/", new FolderRequestBody(user, destFolder), String.class);

        //upload file
        uploadSingleFile(getMultiValueMapHttpEntity());
        //when
        ResponseEntity<String> moveResponse = restTemplate.postForEntity("/file/move", new MoveRequest(user, testFileName, destFolder), String.class);

        //then
        assertEquals(HttpStatus.OK, moveResponse.getStatusCode());

    }

    private ResponseEntity<String> uploadSingleFile(HttpEntity<MultiValueMap<String, Object>> MultiValueMapHttpEntity) {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = MultiValueMapHttpEntity;

        return restTemplate.postForEntity(
                "/file/upload",
                requestEntity,
                String.class);
    }

    private static HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource(testFileName));
        body.add("user", user);

        return new HttpEntity<>(body, headers);
    }


}
