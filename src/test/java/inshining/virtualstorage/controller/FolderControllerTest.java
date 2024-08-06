package inshining.virtualstorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import inshining.virtualstorage.dto.FolderRenameRequest;
import inshining.virtualstorage.dto.MoveRequest;
import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderRequestBody;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.service.FolderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FolderController.class)
public class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FolderService folderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateFolder_Success() throws Exception {
        String username = "user";
        String folderName = "folderName";
        FolderCreateResponse folderCreateResponse = new FolderCreateResponse("user", "folderName", "parentPath/");
        String content = objectMapper.writeValueAsString(new FolderRequestBody(username, folderName));
        when(folderService.createFolder(username, folderName)).thenReturn(folderCreateResponse);
        when(folderService.createFolder("user", "parentPath/folderName")).thenReturn(folderCreateResponse);

        mockMvc.perform(post("/folder/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
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

    @Test
    void testRenameFolder_Success() throws Exception {
        String username = "user";
        String folderName = "folderName";
        String newFolderName = "newFolderName";
        FolderCreateResponse folderCreateResponse = new FolderCreateResponse("user", "newFolderName", "parentPath/");
        String content = objectMapper.writeValueAsString(new FolderRenameRequest(username, folderName,newFolderName));
        when(folderService.renameFolder(username, folderName, newFolderName)).thenReturn(folderCreateResponse);

        mockMvc.perform(put("/folder/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value(folderCreateResponse.ownerName()))
                .andExpect(jsonPath("$.folderName").value(folderCreateResponse.folderName()))
                .andExpect(jsonPath("$.path").value(folderCreateResponse.path())
                );
    }

    @Test
    void testRenameDuplicatedFolder_Fail() throws Exception {
        String username = "user";
        String folderName = "folderName";
        String newFolderName = "newFolderName";
        when(folderService.renameFolder(username, folderName, newFolderName)).thenThrow(DuplicateFileNameException.class);

        mockMvc.perform(put("/folder/")
                .param("userName", username)
                .param("originFolderName", folderName)
                .param("targetFolderName", newFolderName))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("폴더 삭제 성공")
    @Test
    void testDeleteFolder_Success() throws Exception {
        String username = "user";
        String folderName = "folderName";
        String content = objectMapper.writeValueAsString(new FolderRequestBody(username, folderName));

        when(folderService.deleteFolder(username, folderName)).thenReturn(true);

        mockMvc.perform(delete("/folder/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("폴더 삭제 실패")
    @Test
    void testDeleteFolder_Fail() throws Exception {
        String username = "user";
        String folderName = "folderName";
        String content = objectMapper.writeValueAsString(new FolderRequestBody(username, folderName));

        when(folderService.deleteFolder(username, folderName)).thenReturn(false);

        mockMvc.perform(delete("/folder/")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("성공: 폴더 이동")
    @Test
    void testMoveFolder_Success() throws Exception {
        String username = "user";
        String folderName = "/folderName";
        String targetFolderName = "/targetFolderName";

        String content = objectMapper.writeValueAsString(new MoveRequest(username, folderName, targetFolderName));

        when(folderService.move(username, folderName, targetFolderName)).thenReturn(true);

        mockMvc.perform(post("/folder/move")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
