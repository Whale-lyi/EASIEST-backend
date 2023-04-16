package com.mickey.sentibackend.iteration2.controller;

import com.mickey.sentibackend.controller.SentiController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SentiControllerFileTest {

    @Autowired
    private SentiController sentiController;

    private MockMvc mockMvc;

    private MockMultipartFile mockFile;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(sentiController).build();
        mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "I hate frogs.\nI love you!".getBytes());
    }

    /**
     * 文件测试中的成功测试, 默认类型，解释, 不对指定列分类
     *
     * @throws Exception
     */
    @Test
    public void fileSuccessTest1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/v1/file")
                        .file(mockFile)
                        .param("type", "default")
                        .param("explain", "true")
                        .param("annotatecol", "0"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", "code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg", "msg").value("成功"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", "data").value("Positive\\tNegative\\tText\\tExplanation\\n1\\t-4\\tI hate frogs.\\tI hate[-4] frogs .[sentence: 1,-4] [result: max + and - of any sentence][overall result = -1 as pos<-neg]\\n4\\t-1\\tI love you!\\tI love[3] you ![+1 punctuation emphasis] [sentence: 4,-1] [result: max + and - of any sentence][overall result = 1 as pos>-neg]\\n"))
                .andDo(MockMvcResultHandlers.print());
    }
}
