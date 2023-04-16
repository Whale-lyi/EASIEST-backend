package com.mickey.sentibackend.service.impl;

import com.mickey.sentibackend.exception.SentiException;
import com.mickey.sentibackend.service.SentiService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.wlv.sentistrength.SentiStrength;
import uk.ac.wlv.utilities.FileOps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SentiServiceImpl implements SentiService {

    /**
     * 方法用于分析文本
     *
     * @param text          待分析文本
     * @param type          分析模式
     * @param explain       是否需要解释
     * @param paragraphMode 段落求值方案
     * @param sentenceMode  句子求值方案
     * @return 分析结果
     */
    public String analyzeText(String text, String type, Boolean explain, String paragraphMode, String sentenceMode) {
        SentiStrength sentiStrength = new SentiStrength();
        ArrayList<String> paramList = new ArrayList<>();
        // 添加字典
        paramList.add("sentidata");
        paramList.add("./src/main/resources/SentStrength_Data/");
//        paramList.add("/home/lighthouse/SentStrength_Data/");
        // 添加模式与求值方案
        addTypeParam(paramList, type, paragraphMode, sentenceMode);
        // 添加解释
        if (Boolean.TRUE.equals(explain)) {
            paramList.add("explain");
        }
        // 列表转数组初始化
        String[] initArray = paramList.toArray(new String[0]);
        sentiStrength.initialise(initArray);

        return sentiStrength.computeSentimentScores(text);
    }

    /**
     * 方法用于分析文件
     *
     * @param file          待分析文件
     * @param type          分析模式
     * @param explain       是否需要解释
     * @param annotatecol   需要分析评注的文本列
     * @param paragraphMode 段落求值方案
     * @param sentenceMode  句子求值方案
     * @return 输出文件路径
     */
    public String analyzeFile(MultipartFile file,
                              String type,
                              Boolean explain,
                              String annotatecol,
                              String upload,
                              String paragraphMode,
                              String sentenceMode) {
        String filename = file.getOriginalFilename();
        String path = upload + "/" + filename;
        File filePath = new File(path);
        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new SentiException(4, "输入文件读取失败");
        }
        SentiStrength sentiStrength = new SentiStrength();
        ArrayList<String> paramList = new ArrayList<>();
        // 添加字典
        paramList.add("sentidata");
        paramList.add("./src/main/resources/SentStrength_Data/");
//        paramList.add("/home/lighthouse/SentStrength_Data/");
        paramList.add("input");
        paramList.add(path);
        if (annotatecol != null && !annotatecol.equals("")) {
            paramList.add("annotatecol");
            paramList.add(annotatecol);
        }
        // 添加模式与求值方案
        addTypeParam(paramList, type, paragraphMode, sentenceMode);
        // 添加解释
        if (explain) {
            paramList.add("explain");
        }
        paramList.add("overwrite");
        // 列表转数组初始化
        String[] initArray = paramList.toArray(new String[0]);
        sentiStrength.initialiseAndRun(initArray);
        StringBuilder res;
        if (Integer.parseInt(annotatecol) == 0) {
            File[] files = new File(upload).listFiles();
            List<File> poss = new ArrayList<>();
            List<Integer> possible = new ArrayList<>();
            for (File f : files) {
                String name = f.getName();
                String extension = "_out.txt";
                String chopExtension = FileOps.s_ChopFileNameExtension(filename);
                if (name.contains(extension)) {
                    poss.add(f);
                    possible.add(Integer.parseInt(name.substring(chopExtension.length(), name.indexOf(extension))));
                }
            }
            int max = -1;
            int index = -1;
            for (int i = 0; i < poss.size(); i++) {
                if (max < possible.get(i)) {
                    max = possible.get(i);
                    index = i;
                }
            }
            path = poss.get(index).getAbsolutePath();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            res = new StringBuilder();
//            res.append(path).append("\n");
            String s = "";
            while ((s = br.readLine()) != null) {
                res.append(s).append("\n");
            }
        } catch (IOException e) {
            throw new SentiException(5, "输出文件读取失败");
        }
        return res.toString();
    }

    /**
     * 方法用于向参数列表添加分析模式与段落句子求值方案
     *
     * @param paramList     参数列表
     * @param type          模式字符串
     * @param paragraphMode 段落求值方案
     * @param sentenceMode  句子求值方案
     */
    private void addTypeParam(List<String> paramList, String type, String paragraphMode, String sentenceMode) {
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
        if (!result) {
            throw new SentiException(1, "分析模式参数错误");
        }

        result = false;
        switch (paragraphMode) {
            case "tot":
                paramList.add("paragraphCombineTot");
                result = true;
                break;
            case "avg":
                paramList.add("paragraphCombineAv");
                result = true;
                break;
            case "max":
                result = true;
                break;
            default:
        }
        if (!result) {
            throw new SentiException(2, "段落求值方案参数错误");
        }

        result = false;
        switch (sentenceMode) {
            case "tot":
                paramList.add("sentenceCombineTot");
                result = true;
                break;
            case "avg":
                paramList.add("sentenceCombineAv");
                result = true;
                break;
            case "max":
                result = true;
                break;
            default:
        }
        if (!result) {
            throw new SentiException(3, "句子求值方案参数错误");
        }
    }
}
