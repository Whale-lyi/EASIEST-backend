package com.mickey.sentibackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface SentiService {

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
    String analyzeText(String text, String type, Boolean explain, String paragraphMode, String sentenceMode);

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
    String analyzeFile(MultipartFile file,
                              String type,
                              Boolean explain,
                              String annotatecol,
                              String upload,
                              String paragraphMode,
                              String sentenceMode);

}
