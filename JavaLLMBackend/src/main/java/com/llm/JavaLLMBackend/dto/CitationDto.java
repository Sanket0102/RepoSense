package com.llm.JavaLLMBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitationDto {

    String filePath;
    Integer startLine;
    Integer endLine;
    String language;

}
