package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.model.MetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

public class MetaDataServiceTest {

    public MultipartFile file;


    @BeforeEach
    void setUp() {
        String fileName = "testfile.txt";
        String content = "Hello, World!";
        String contentType = "text/plain";

        file = new MockMultipartFile(
                "file",
                fileName,
                contentType,
                content.getBytes()
        );
    }

    @Test
    void createMetadataObjectTest(){
        // Get the file and save it somewhere
        UUID uuid1 = UUID.randomUUID();
        String username = "testuser";
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        MetaData metaData = new MetaData(uuid1, username, contentType, originalFilename, size);
        assertEquals(uuid1, metaData.getId());
        assertEquals(username, metaData.getUsername());
        assertEquals(contentType, metaData.getContentType());
        assertEquals(originalFilename, metaData.getOriginalFilename());
        assertEquals(size, metaData.getSize());
    }
}
