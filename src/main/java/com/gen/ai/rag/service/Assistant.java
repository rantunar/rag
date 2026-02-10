package com.gen.ai.rag.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {

    @SystemMessage("you are a helpful assistant")
    String answer(@UserMessage String message);
}
