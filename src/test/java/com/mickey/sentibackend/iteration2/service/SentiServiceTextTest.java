package com.mickey.sentibackend.iteration2.service;

import com.mickey.sentibackend.exception.SentiException;
import com.mickey.sentibackend.service.SentiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SentiServiceTextTest {

    @Autowired
    SentiService sentiService;

    /**
     * default, explain.
     */
    @Test
    public void textSuccessTest1() {
        String result = sentiService.analyzeText("I hate frogs.", "default", true, "max", "max");
        Assert.assertEquals("1 -4 I hate[-4] frogs .[sentence: 1,-4] [result: max + and - of any sentence][overall result = -1 as pos<-neg]", result);
    }

    /**
     * default.
     */
    @Test
    public void textSuccessTest2() {
        String result = sentiService.analyzeText("I hate frogs.", "default", false, "max", "max");
        Assert.assertEquals("1 -4", result);
    }

    /**
     * trinary, explain.
     */
    @Test
    public void textSuccessTest3() {
        String result = sentiService.analyzeText("I hate frogs.", "trinary", true, "max", "max");
        Assert.assertEquals("1 -4 -1 I hate[-4] frogs .[sentence: 1,-4] [result: max + and - of any sentence][overall result = -1 as pos<-neg]", result);
    }

    /**
     * binary.
     */
    @Test
    public void textSuccessTest4() {
        String result = sentiService.analyzeText("I hate frogs.", "binary", false, "max", "max");
        Assert.assertEquals("1 -4 -1", result);
    }

    /**
     * scale, explain.
     */
    @Test
    public void textSuccessTest5() {
        String result = sentiService.analyzeText("I hate frogs.", "scale", true, "max", "max");
        Assert.assertEquals("1 -4 -3 I hate[-4] frogs .[sentence: 1,-4] [result: max + and - of any sentence][scale result = sum of pos and neg scores]", result);
    }

    /**
     * default, explain.
     */
    @Test
    public void textExceptionTest() {
        try {
            sentiService.analyzeText("I hate frogs.", "error_type", true, "max", "max");
        } catch (SentiException e) {
            log.info("The code should go here");
            Assert.assertEquals("1", String.valueOf(e.getCode()));
            Assert.assertEquals("分析模式参数错误", e.getMessage());
        }
    }
}
