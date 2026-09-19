package org.web.fitness.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CoachTool {
    @Tool(name = "get_coach",description = "健身教练提供健身建议")
    public String getCoach(@ToolParam(description = "运动名称") String coachName){
        System.out.println("调用健身工具");
        // 模拟健身查询（实际可对接API）
        return String.format("%s当前运动：晴，温度22℃，湿度45%%。", coachName);
    }
}
