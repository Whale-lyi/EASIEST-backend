package com.mickey.sentibackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mickey.sentibackend.entity.Comment;
import com.mickey.sentibackend.entity.Issue;
import com.mickey.sentibackend.exception.SentiException;
import com.mickey.sentibackend.service.IssueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IssueServiceImpl implements IssueService {

    private static final String BASE_URL = "https://api.github.com/repos/";
    private static final String ACCESS_TOKEN = "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";

    /**
     * 获取Issue及其评论
     * @param url github仓库url
     * @param state 爬取的issue的状态
     * @return issue列表
     */
    @Override
    public List<Issue> getIssues(String url, String state) {
        String apiURL = parseURL(url, state);
        log.info(apiURL);
        JSONArray issues = JSON.parseArray(makeApiRequest(apiURL));
        List<Issue> issueList = new ArrayList<>();
        for (int i = 0; i < issues.size(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            Long issueId = issue.getLong("id");
            String userName = issue.getJSONObject("user").getString("login");
            String title = issue.getString("title");
            String issueBody = issue.getString("body");
            Date createTime = parseDate(issue.getString("created_at"));
            // 获取issue对应comment
            List<Comment> commentList = new ArrayList<>();
            Integer commentNum = issue.getInteger("comments");
            if (commentNum > 0) {
                String commentsURL = issue.getString("comments_url");
                JSONArray comments = JSON.parseArray(makeApiRequest(commentsURL));
                for (int j = 0; j < comments.size(); j++) {
                    JSONObject comment = comments.getJSONObject(j);
                    Long commentId = comment.getLong("id");
                    String user = comment.getJSONObject("user").getString("login");
                    String commentBody = comment.getString("body");
                    Date createAt = parseDate(comment.getString("created_at"));
                    commentList.add(new Comment(commentId, user, commentBody, createAt));
                }
            }

            issueList.add(new Issue(issueId, userName, title, issueBody, createTime, commentList));
        }

        return issueList;
    }

    /**
     * 发起api请求
     * @param apiURL 请求url
     * @return 响应体
     */
    private String makeApiRequest(String apiURL) {
        HttpGet httpGet = new HttpGet(apiURL);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "token " + ACCESS_TOKEN);
        httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
        // 发起请求
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new SentiException(8, "无法从API获取数据: " + httpResponse.getStatusLine().getReasonPhrase());
            }
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            throw new SentiException(9, "发生IO异常");
        }
    }

    /**
     * 解析仓库字符串, 生成apiURL
     * @param repositoryURL 仓库url
     * @param state 爬取的issue的状态
     * @return api请求的url
     */
    private String parseURL(String repositoryURL, String state) {
        StringBuilder apiURL = new StringBuilder(BASE_URL);
        // 使用正则处理仓库URL, 获取所有者及仓库名, 以及可能的里程碑号
        Pattern pattern = Pattern.compile("https://github.com/(\\w+)/([\\w-]+)(?:/milestone/(\\d+))?/?");
        Matcher matcher = pattern.matcher(repositoryURL);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            String milestone = matcher.group(3);
            // 验证仓库有没有开启issue
            if (!checkIssue(owner, repo)) {
                throw new SentiException(10, "该仓库没有开启issue功能");
            }
            // 拼接issue状态到apiURL
            apiURL.append(owner).append('/').append(repo).append('/').append("issues");
            if ("all".equals(state) || "closed".equals(state)) {
                apiURL.append("?state=").append(state);
            } else {
                apiURL.append("?state=").append("open");
            }
            // 拼接里程碑号到apiURL
            if (milestone != null) {
                apiURL.append("&milestone=").append(milestone);
            }
        } else {
            throw new SentiException(6, "输入url不符合规定");
        }

        return apiURL.toString();
    }

    /**
     * 解析时间字符串
     * @param dateString 时间字符串
     * @return 时间Date对象
     */
    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new SentiException(7, "时间格式转换错误");
        }
    }

    /**
     * 检查仓库是否开启issue功能
     * @param owner 仓库拥有者
     * @param repo 仓库名
     * @return 是否开启issue功能
     */
    private boolean checkIssue(String owner, String repo) {
        JSONObject repoInfo = JSON.parseObject(makeApiRequest(BASE_URL + owner + '/' + repo));
        return repoInfo.getBoolean("has_issues");
    }
}
