package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkResult {
    /**
     * 方面
     */
    String aspect;
    /**
     * 标注原文, 其中方面用【】标注
     */
    String sentence;
}
