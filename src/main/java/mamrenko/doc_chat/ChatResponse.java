package mamrenko.doc_chat;

import java.util.List;

public record ChatResponse(String answer, List<Source> sources) {}
