package com.mickey.sentibackend.iteration2.controller;

import com.mickey.sentibackend.controller.SentiController;
import com.mickey.sentibackend.exception.SentiException;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 文本测试类
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SentiControllerTextTest {

    @Autowired
    private SentiController sentiController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(sentiController).build();
    }

    /**
     * 文本测试中的成功测试, 默认类型，不解释
     *
     * @throws Exception
     */
    @Test
    public void textSuccessTest1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/text")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("text", "I hate frogs.")
                        .param("type", "default")
                        .param("explain", "false")
                        .param("paragraphMode", "max")
                        .param("sentenceMode", "max")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", "code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg", "msg").value("成功"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", "data").value("1 -4"))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 文本测试中的成功测试, 默认类型，解释
     *
     * @throws Exception
     */
    @Test
    public void textSuccessTest2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/text")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("text", "I hate frogs.")
                        .param("type", "default")
                        .param("explain", "true")
                        .param("paragraphMode", "max")
                        .param("sentenceMode", "max")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", "code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg", "msg").value("成功"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("1 -4 I hate[-4] frogs")))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 文本测试中的成功测试, trinary类型
     *
     * @throws Exception
     */
    @Test
    public void textSuccessTest3() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/text")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("text", "I hate frogs.")
                        .param("type", "trinary")
                        .param("explain", "false")
                        .param("paragraphMode", "max")
                        .param("sentenceMode", "max")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", "code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg", "msg").value("成功"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", "data").value("1 -4 -1"))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 文本测试中的成功测试, binary类型
     *
     * @throws Exception
     */
    @Test
    public void textSuccessTest4() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/text")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("text", "I hate frogs.")
                        .param("type", "binary")
                        .param("explain", "false")
                        .param("paragraphMode", "max")
                        .param("sentenceMode", "max")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", "code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg", "msg").value("成功"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", "data").value("1 -4 -1"))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 文本测试中的成功测试, scale类型
     *
     * @throws Exception
     */
    @Test
    public void textSuccessTest5() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/text")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("text", "I hate frogs.")
                        .param("type", "scale")
                        .param("explain", "true")
                        .param("paragraphMode", "max")
                        .param("sentenceMode", "max")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", "code").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg", "msg").value("成功"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data", "data").value("1 -4 -3 I hate[-4] frogs .[sentence: 1,-4] [result: max + and - of any sentence][scale result = sum of pos and neg scores]"))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * 文本测试中的异常测试, 异常类型为 1, 分析模式参数错误
     */
    @Test
    public void textExceptionTest() {
        try {
            sentiController.analyzeText("I hate frogs.", "error_type", true, "max", "max");
        } catch (SentiException e) {
            log.info("The code should go here");
            Assert.assertEquals("1", String.valueOf(e.getCode()));
            Assert.assertEquals("分析模式参数错误", e.getMessage());
        }
    }

}
