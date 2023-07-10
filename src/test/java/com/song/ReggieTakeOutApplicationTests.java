package com.song;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class ReggieTakeOutApplicationTests {

    @Test
    void contextLoads() {
        String img = "eadada.jpg";
        String substring = img.substring(img.indexOf("a"));
        System.out.println(substring);
    }

}
