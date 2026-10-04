package com.llm.JavaLLMBackend.indexing;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;


import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeContextRetriever {

    private static final String NO_MATCHES = "(No matching code chunks found)";

    private final VectorStore vectorStore;
    private final CitationMapper citationMapper;

    public RetrieveContext retrieveContext(UUID repositoryId, String question){
        var filter = new FilterExpressionBuilder()
                .eq(RagSettings.METADATA_REPO_ID,repositoryId.toString())
                .build();

        var search = SearchRequest.builder()
                .query(question)
                .topK(RagSettings.TOP_K_CHUNKS)
                .filterExpression(filter)
                .build();

        var documents = vectorStore.similaritySearch(search);

        documents.stream()
                .forEach(doc ->{
                    System.out.println("Document Id :-" + doc.getId());
                    System.out.println("Document Text :-" + doc.getText());
                    System.out.println("Document Formatted :-" + doc.getFormattedContent());
                    System.out.println("Document Metadata :-" + doc.getMetadata());
                }) ;

        var citations = documents.stream().map(CitationMapper::fromDocument)
                .distinct()
                .toList();

        String contextText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        if(contextText.isBlank()){
            contextText = NO_MATCHES;
        }

        return RetrieveContext.builder()
                .contextText(contextText)
                .citationDtoList(citations)
                .build();
    }

}
