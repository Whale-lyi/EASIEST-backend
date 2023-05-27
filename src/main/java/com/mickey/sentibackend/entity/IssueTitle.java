package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueTitle {
    /**
     * 标题内容
     */
    private String title;
    /**
     * 标题情绪分数
     */
    private Integer titleScore;
}
