package inshining.virtualstorage.metadata.controller;

import exception.DuplicateFileNameException;
import inshining.virtualstorage.controller.FolderController;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.service.FolderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FolderController.class)
public class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FolderService folderService;

    @Test
    void testCreateFolder_Success() throws Exception {
        FolderCreateResponse folderCreateResponse = new FolderCreateResponse("user", "folderName", "parentPath/");
        when(folderService.createFolder("user", "parentPath/folderName")).thenReturn(folderCreateResponse);

        mockMvc.perform(post("/folder/")
                .param("user", "user")
                .param("folderName", "parentPath/folderName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value(folderCreateResponse.ownerName()))
                .andExpect(jsonPath("$.folderName").value(folderCreateResponse.folderName()))
                .andExpect(jsonPath("$.path").value(folderCreateResponse.path())
                );
    }

    @Test
    void duplicatedFolder_Then_fail() throws Exception{
        // given
        String username = "user";
        String folderName = "folder1";
        when(folderService.createFolder(username, folderName)).thenThrow(DuplicateFileNameException.class);

        mockMvc.perform(post("/folder/")
                .param("user", username)
                .param("folderName", folderName))
                .andExpect(status().isBadRequest());
    }
}
