package com.llm.JavaLLMBackend.indexing;

import com.llm.JavaLLMBackend.dto.CitationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrieveContext {
    List<CitationDto> citationDtoList;
    String contextText;
}
