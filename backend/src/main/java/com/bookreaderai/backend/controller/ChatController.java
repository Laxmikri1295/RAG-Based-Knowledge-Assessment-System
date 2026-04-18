package com.bookreaderai.backend.controller;

import com.bookreaderai.backend.dto.ChatRequest;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/getaianswer")

//@NoArgsConstructor(force = true)
public class ChatController {
    private final OllamaChatModel ollamaChatModel;
    public ChatController(OllamaChatModel ollamaChatModel){
        this.ollamaChatModel= ollamaChatModel;
    }
    @PostMapping
    public String chat(@RequestBody ChatRequest chatRequest){
        Prompt prompt = new Prompt(chatRequest.getMessage());

        ChatResponse response = ollamaChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
