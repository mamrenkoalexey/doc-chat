package mamrenko.doc_chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock ChatClient.Builder chatClientBuilder;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;
    @Mock VectorStore vectorStore;
    @Mock JdbcTemplate jdbc;

    IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        ingestionService = new IngestionService(vectorStore, chatClientBuilder, jdbc);
    }

    @Test
    void ingest_textFile_addedToVectorStore() throws Exception {
        var file = new MockMultipartFile("file", "notes.txt", "text/plain",
                "Hello world, this is a test document.".getBytes());

        ingestionService.ingest(file);

        verify(vectorStore).add(anyList());
    }

    @Test
    void ingest_imageFile_callsVisionApi() throws Exception {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("A diagram showing architecture");

        var file = new MockMultipartFile("file", "diagram.png", "image/png", new byte[]{1, 2, 3});

        ingestionService.ingest(file);

        verify(chatClient).prompt();
        verify(vectorStore).add(anyList());
    }

    @Test
    void ingest_alwaysDeletesOldChunksFirst() throws Exception {
        var file = new MockMultipartFile("file", "report.txt", "text/plain", "Content".getBytes());

        ingestionService.ingest(file);

        verify(jdbc).update(anyString(), eq("report.txt"));
    }
}
