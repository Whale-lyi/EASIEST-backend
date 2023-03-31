package com.mickey.sentibackend.service;

import com.mickey.sentibackend.exception.SentiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.wlv.sentistrength.SentiStrength;
import uk.ac.wlv.utilities.FileOps;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class SentiService {

    /**
     * 方法用于分析文本
     * @param text 待分析文本
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
     * 方法用于分析文件
     * @param file 待分析文件
     * @param type 分析模式
     * @param explain 是否需要解释
     * @param annotatecol 需要分析评注的文本列
     * @return 输出文件路径
     */
    public String analyzeFile(MultipartFile file, String type, Boolean explain, String annotatecol, String upload) {
        String filename = file.getOriginalFilename();
        String path = upload + "/" + filename;
        File filePath = new File(path);
        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new SentiException(1, "输入文件读取失败");
        }
        SentiStrength sentiStrength = new SentiStrength();
        ArrayList<String> paramList = new ArrayList<>();
        // 添加字典
        paramList.add("sentidata");
//        paramList.add("./src/main/resources/SentStrength_Data/");
        paramList.add("/home/lighthouse/SentStrength_Data/");
        paramList.add("input");
        paramList.add(path);
        if (annotatecol != null && !annotatecol.equals("")) {
            paramList.add("annotatecol");
            paramList.add(annotatecol);
        }
        // 添加模式
        if (!addTypeParam(paramList, type)) {
            throw new SentiException(1, "分析模式参数错误");
        }
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
            throw new SentiException(1, "输出文件读取失败");
        }
        return res.toString();
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
