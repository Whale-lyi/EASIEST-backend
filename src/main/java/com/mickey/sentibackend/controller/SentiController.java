package com.mickey.sentibackend.controller;

import com.mickey.sentibackend.entity.Result;
import com.mickey.sentibackend.service.SentiService;
import com.mickey.sentibackend.exception.SentiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.wlv.sentistrength.SentiStrength;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1")
@Slf4j
@CrossOrigin
public class SentiController {

    private final SentiService sentiService;

    @Autowired
    public SentiController(SentiService sentiService) {
        this.sentiService = sentiService;
    }

    /**
     * 用于处理解析文本的请求
     * @param text 待分析文本
     * @param type 分析模式
     * @param explain 是否需要解释
     * @return 分析结果
     */
    @PostMapping("/text")
    public Result<String> analyzeText(@RequestParam("text") String text,
                                      @RequestParam("type") String type,
                                      @RequestParam("explain") Boolean explain) {
        String result = sentiService.analyzeText(text, type, explain);
        return Result.buildSuccess(result);
    }

    /**
     * 用于处理解析文件的请求
     * @param file 待分析文件
     * @param type 分析模式
     * @param explain 是否需要解释
     * @param textcol 文本所在列
     * @param idcol id所在列
     * @return 分析结果
     */
    @PostMapping("/file")
    public Result<String> analyzeFile(@RequestParam MultipartFile file,
                                             @RequestParam("type") String type,
                                             @RequestParam("explain") Boolean explain,
                                             @RequestParam("textcol") String textcol,
                                             @RequestParam("idcol") String idcol) {
        String res = sentiService.analyzeFile(file, type, explain, textcol, idcol);
        return Result.buildSuccess(res);
    }

    /**
     * 该方法仅用于测试连接和jar包的使用
     */
    @GetMapping("/conn-test")
    public Result<String> testConn() {
        return Result.buildSuccess("Hello SentiStrength");
    }

    /**
     * 该方法仅用于测试jar包的使用
     */
    @GetMapping("/jar-test")
    public Result<String> testSentiStrength(@RequestParam("text") String text) {
        SentiStrength sentiStrength = new SentiStrength();
        // 本地情绪字典
//        String[] ssthInitialisation = {"sentidata", "./src/main/resources/SentStrength_Data/", "explain"};
        // 服务器字典路径
        String[] ssthInitialisation = {"sentidata", "/home/lighthouse/SentStrength_Data/", "explain"};
        sentiStrength.initialise(ssthInitialisation);
        return Result.buildSuccess(sentiStrength.computeSentimentScores(text));
    }

}
