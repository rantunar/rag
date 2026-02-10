package com.gen.ai.rag.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {

    @NonNull private final ChatModel chatModel;
    @NonNull private final ContentRetriever hmpContentRetriever;
    @NonNull private final ContentRetriever jdContentRetriever;

    public String chat(String message) {
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(hmpContentRetriever)
                .build();
        return assistant.answer(message);
    }

    public String chatUsingJd(String message) {
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(jdContentRetriever)
                .build();
        return assistant.answer(message);
    }
}
