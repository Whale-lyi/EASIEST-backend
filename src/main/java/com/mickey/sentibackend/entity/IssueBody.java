package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueBody {
    /**
     * 内容
     */
    private String body;
    /**
     * 内容情绪分数
     */
    private Integer bodyScore;
}
