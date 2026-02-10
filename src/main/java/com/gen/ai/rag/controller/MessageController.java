package com.gen.ai.rag.controller;

import com.gen.ai.rag.model.MessageForm;
import com.gen.ai.rag.service.PolicyService;
import dev.langchain4j.data.message.ChatMessage;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class MessageController {

    private final PolicyService policyService;

    private static final Map<String, List<ChatMessage>> userChatMap = new HashMap<>();

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/message")
    public String showForm(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        model.addAttribute("messageForm", new MessageForm());
        return "message";
    }

    @PostMapping("/ai/message")
    public String submitMessage(@ModelAttribute MessageForm form, Model model,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        String reply = policyService.chat(form.getMessage());
        model.addAttribute("reply", reply);
        return "message";
    }

    @PostMapping("/ai/jd/message")
    public String submitMessageForJd(@ModelAttribute MessageForm form, Model model,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        String reply = policyService.chatUsingJd(form.getMessage());
        model.addAttribute("reply", reply);
        return "message";
    }
}
