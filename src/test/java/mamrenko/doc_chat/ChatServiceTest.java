package mamrenko.doc_chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ChatClient.Builder chatClientBuilder;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;
    @Mock VectorStore vectorStore;

    ChatService chatService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        chatService = new ChatService(chatClientBuilder, vectorStore);
    }

    void mockChatClientReturns(String answer) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(answer);
    }

    @Test
    void chat_returnsAnswerFromChatClient() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        mockChatClientReturns("Here is the answer");

        ChatResponse response = chatService.chat("What is this?", List.of("file.pdf"));

        assertThat(response.answer()).isEqualTo("Here is the answer");
    }

    @Test
    void chat_sourcesAreDeduplicatedByFilename() {
        var chunk1 = new Document("text 1", Map.of("filename", "cv.pdf"));
        var chunk2 = new Document("text 2", Map.of("filename", "cv.pdf"));
        var chunk3 = new Document("text 3", Map.of("filename", "report.docx"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(chunk1, chunk2, chunk3));
        mockChatClientReturns("answer");

        ChatResponse response = chatService.chat("question", List.of("cv.pdf", "report.docx"));

        assertThat(response.sources())
                .extracting(Source::filename)
                .containsExactlyInAnyOrder("cv.pdf", "report.docx");
    }

    @Test
    void chat_noMatchingDocs_returnsEmptySources() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        mockChatClientReturns("I don't know");

        ChatResponse response = chatService.chat("question", List.of("missing.pdf"));

        assertThat(response.sources()).isEmpty();
    }
}
