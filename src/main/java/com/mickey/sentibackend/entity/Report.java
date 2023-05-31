package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    /**
     * 标注结果
     */
    List<MarkResult> markResultList;
    /**
     * 报告内容
     */
    String content;

}
