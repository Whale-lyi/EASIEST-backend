package com.mickey.sentibackend.service;

import com.mickey.sentibackend.exception.SentiException;
import org.springframework.stereotype.Service;
import uk.ac.wlv.sentistrength.SentiStrength;

import java.util.ArrayList;
import java.util.List;

@Service
public class SentiService {

    /**
     * 方法用于分析文本
     * @param text 带分析文本
     * @param type 分析模式
     * @param explain 是否需要解释
     * @return 分析结果
     */
    public String analyzeText(String text, String type, Boolean explain) {
        SentiStrength sentiStrength = new SentiStrength();
        ArrayList<String> paramList = new ArrayList<>();
        // 添加字典
        paramList.add("sentidata");
//        paramList.add("./src/main/resources/SentStrength_Data/");
        paramList.add("/home/lighthouse/SentStrength_Data/");
        // 添加模式
        if (!addTypeParam(paramList, type)) {
            throw new SentiException(1, "分析模式参数错误");
        }
        // 添加解释
        if (explain) {
            paramList.add("explain");
        }
        // 列表转数组初始化
        String[] initArray = paramList.toArray(new String[0]);
        sentiStrength.initialise(initArray);

        return sentiStrength.computeSentimentScores(text);
    }


    /**
     * 方法用于向参数列表添加分析模式
     * @param paramList 参数列表
     * @param type 模式字符串
     * @return 模式是否合法
     */
    private boolean addTypeParam(List<String> paramList, String type) {
        boolean result = false;
        switch (type) {
            case "trinary":
                paramList.add("trinary");
                result = true;
                break;
            case "binary":
                paramList.add("binary");
                result = true;
                break;
            case "scale":
                paramList.add("scale");
                result = true;
                break;
            case "default":
                result = true;
                break;
            default:
        }
        return result;
    }
}
