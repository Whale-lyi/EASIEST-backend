package com.mickey.sentibackend.util;

import uk.ac.wlv.sentistrength.SentiStrength;

import java.util.ArrayList;

public class SentiUtil {

    private static SentiStrength sentiStrength;

    static {
        sentiStrength = new SentiStrength();
        ArrayList<String> paramList = new ArrayList<>();
        paramList.add("sentidata");
        paramList.add("./src/main/resources/SentStrength_Data/");
//        paramList.add("/home/lighthouse/SentStrength_Data/");
        paramList.add("trinary");
        String[] initArray = paramList.toArray(new String[0]);
        sentiStrength.initialise(initArray);
    }

    public static Integer calSentimentScores(String text) {
        String scores = sentiStrength.computeSentimentScores(text);
        String[] split = scores.split(" ");
        return Integer.parseInt(split[2]);
    }

}
