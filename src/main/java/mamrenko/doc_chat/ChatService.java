package mamrenko.doc_chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public ChatResponse chat(String question, List<String> filenames) {
        String filter = filenames.stream()
                .map(f -> "filename == '" + f + "'")
                .collect(Collectors.joining(" OR "));

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .filterExpression(filter)
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);
        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                Answer the question using only the context below.

                Context:
                %s

                Question: %s
                """.formatted(context, question);

        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        List<Source> sources = docs.stream()
                .map(doc -> (String) doc.getMetadata().get("filename"))
                .distinct()
                .map(Source::new)
                .toList();

        return new ChatResponse(answer, sources);
    }
}
