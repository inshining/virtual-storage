package inshining.virtualstorage.metadata.controller;

import inshining.virtualstorage.controller.MetaDataController;
import inshining.virtualstorage.dto.FileUploadResponse;
import inshining.virtualstorage.service.MetaDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(MetaDataController.class)
public class MetaDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetaDataService metaDataService;

    @Test
    public void testUpload() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        when(metaDataService.uploadFile(file, "test")).thenReturn(new FileUploadResponse(true, "File uploaded successfully"));

        mockMvc.perform(
                multipart("/file/upload").file(file)
                .param("user", "test")
                ).andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    public void testUploadFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        // Mock the service method to return false (failed upload)
        when(metaDataService.uploadFile(any(MultipartFile.class), eq("test"))).thenReturn(new FileUploadResponse(false, "Failed to upload file"));

        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .param("user", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to upload file"));
    }

    @Test
    public void testUploadEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .param("user", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please select a file to upload"));
    }
}
