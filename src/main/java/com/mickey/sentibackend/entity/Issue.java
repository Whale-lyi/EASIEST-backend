package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Issue {
    /**
     * issue的唯一编号
     */
    private Long id;
    /**
     * 用户
     */
    private String user;
    /**
     * 标题
     */
    private IssueTitle title;
    /**
     * 内容
     */
    private List<IssueBody> bodyList;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 评论列表
     */
    private List<Comment> commentList;
}
