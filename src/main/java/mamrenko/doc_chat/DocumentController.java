package mamrenko.doc_chat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final IngestionService ingestionService;

    public DocumentController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        ingestionService.ingest(file);
        return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
    }

    @GetMapping
    public List<String> list() {
        return ingestionService.getUploadedFiles();
    }
}
