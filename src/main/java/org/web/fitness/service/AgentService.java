package org.web.fitness.service;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.web.fitness.config.AgentGraphConfig;
import org.web.fitness.tool.CoachTool;

@Service
public class AgentService {
    private final AgentGraphConfig.AgentWorkflow agentWorkflow;
    public AgentService(AgentGraphConfig.AgentWorkflow agentWorkflow) {
        this.agentWorkflow = agentWorkflow;
    }

    public String ask(String question) {
        return agentWorkflow.execute(question);
    }
}
