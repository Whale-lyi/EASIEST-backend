package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    /**
     * 评论的唯一编号
     */
    private Long id;
    /**
     * 用户
     */
    private String user;
    /**
     * 评论内容
     */
    private String body;
    /**
     * 创建时间
     */
    private Date createTime;
}

