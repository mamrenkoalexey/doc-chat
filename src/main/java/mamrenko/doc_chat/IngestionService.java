package mamrenko.doc_chat;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IngestionService {

    private static final Set<String> IMAGE_EXTENSIONS =
            Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp", "tiff", "tif");

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final TokenTextSplitter splitter = new TokenTextSplitter();
    private final List<String> uploadedFiles = new ArrayList<>();

    public IngestionService(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
    }

    public void ingest(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        String text = extractText(file);

        Document document = new Document(text, Map.of("filename", filename));
        List<Document> chunks = splitter.apply(List.of(document));
        vectorStore.add(chunks);

        if (!uploadedFiles.contains(filename)) {
            uploadedFiles.add(filename);
        }
    }

    private String extractText(MultipartFile file) throws Exception {
        String ext = extension(file.getOriginalFilename());
        if (IMAGE_EXTENSIONS.contains(ext)) {
            return describeImage(file);
        }
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());
        try (InputStream is = file.getInputStream()) {
            parser.parse(is, handler, metadata);
        }
        return handler.toString().trim();
    }

    private String describeImage(MultipartFile file) throws Exception {
        MimeType mimeType = MimeType.valueOf(
                file.getContentType() != null ? file.getContentType() : "image/jpeg");
        ByteArrayResource resource = new ByteArrayResource(file.getBytes());
        return chatClient.prompt()
                .user(u -> u
                        .text("Describe the content of this image in full detail. Include all visible text, objects, scenes, and any information useful for answering questions about it.")
                        .media(mimeType, resource))
                .call()
                .content();
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public List<String> getUploadedFiles() {
        return uploadedFiles;
    }
}
