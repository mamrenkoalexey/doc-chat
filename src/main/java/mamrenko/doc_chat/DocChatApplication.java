package mamrenko.doc_chat;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocChatApplication {

	public static void main(String[] args) {
		Dotenv.configure().ignoreIfMissing().load().entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		SpringApplication.run(DocChatApplication.class, args);
	}

}
