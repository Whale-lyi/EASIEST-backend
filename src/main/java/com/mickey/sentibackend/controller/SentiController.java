package com.mickey.sentibackend.controller;

import com.mickey.sentibackend.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.wlv.sentistrength.SentiStrength;

/**
 * @author 刘屿
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class SentiController {

    /**
     * 该方法仅用于测试连接和jar包的使用
     */
    @GetMapping("/test")
    public Result<String> testConn() {
        SentiStrength sentiStrength = new SentiStrength();
        String[] ssthInitialisation = {"sentidata", "./src/main/resources/SentStrength_Data/", "explain"};
        sentiStrength.initialise(ssthInitialisation);
        log.info(sentiStrength.computeSentimentScores("I hate frogs."));
        return Result.buildSuccess("Hello SentiStrength");
    }

}
