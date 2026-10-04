package com.llm.JavaLLMBackend.indexing;

import org.springframework.stereotype.Component;


import java.util.Locale;
import java.util.Set;

@Component
public class CodeFileFilter {

    public static final Set<String> SKIP_DIR = Set.of(
      "node_modules",".git","dist","build","target",".next","vendor",
      "__pycache__",".idea",".vscode","coverage","out"
    );

    public static final Set<String> ALLOWED_EXTENSION = Set.of(
            "java","kt","kts","scala","ts","tsx","js","jsx","mjs","cjs",
            "py","go","rs","rb","php","c","h","cpp","hpp","cs","swift","m","mm",
            "md","mdx","txt","yml","yaml","json","toml","xml","properties","gradle",
            "sql","sh","bash","zsh","dockerfile","makefile","html","css","scss","sass",
            "vue","svelte"
    );

    public static final Set<String> SKIP_FILE_NAMES = Set.of(
            "package-lock.json",
            "yarn.lock",
            "composer.lock",
            "cargo.lock",
            "poetry.lock"
    );

    public boolean isEligible(String path, long sizeBytes , long maxSizeBytes){

        if(path == null || path.isEmpty()){
            return false;
        }

        String normalized = path.replace("\\","/");
        String lower = normalized.toLowerCase(Locale.ROOT);

        for(String s : lower.split("/")){
            if(SKIP_DIR.contains(s)){
                return false;
            }
        }
        String fileName = lower.substring(lower.lastIndexOf("/")+1);
        if(SKIP_FILE_NAMES.contains(fileName)){
            return false;
        }
        if(fileName.contains(".")) {
            if (!ALLOWED_EXTENSION.contains(fileName.split("\\.")[1])) {
                return false;
            }
        }
        return true;
    }

    public String detectLanguage(String filePath){
        String lower = filePath.toLowerCase(Locale.ROOT);
        String fileName = filePath.substring(filePath.lastIndexOf("/")+1);

        if("dockerfile".equalsIgnoreCase(fileName)){
            return "dockerfile";
        }
        if("makefile".equalsIgnoreCase(fileName)){
            return "makefile";
        }
        if(fileName.lastIndexOf('.') < 0){
            return "text";
        }
        return fileName.substring(fileName.lastIndexOf('.')+1);
    }
}
