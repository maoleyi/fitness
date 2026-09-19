package org.web.fitness.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web.fitness.service.AgentService;

@RestController
@RequestMapping("/ai")
public class WebController {


//    private final ChatClient chatClient;
//
//    public WebController(ChatClient.Builder chatClient) {
//        this.chatClient = chatClient.build();
//    }
    private final AgentService agentService;
    public WebController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping(value = "/speak")
    public Object speak(String message){
        return agentService.ask(message);
    }
}
