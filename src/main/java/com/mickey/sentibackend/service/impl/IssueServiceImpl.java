package com.mickey.sentibackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mickey.sentibackend.entity.Comment;
import com.mickey.sentibackend.entity.Issue;
import com.mickey.sentibackend.entity.IssueBody;
import com.mickey.sentibackend.entity.IssueTitle;
import com.mickey.sentibackend.entity.MarkResult;
import com.mickey.sentibackend.entity.Report;
import com.mickey.sentibackend.exception.SentiException;
import com.mickey.sentibackend.service.IssueService;
import com.mickey.sentibackend.util.SentiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class IssueServiceImpl implements IssueService {

    private static final String API_BASE_URL = "https://api.github.com/repos/";
    private static final String ACCESS_TOKEN = "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";
    private static final String MARK_FILE = "/home/lighthouse/mark_res.txt";
    private static final String REPORT_FILE = "/home/lighthouse/Report.md";
    private String[] timeByVersion;

    /**
     * 获取Issue及其评论
     * @param url github仓库url
     * @param state 爬取的issue的状态
     * @param version 仓库版本名
     * @return issue列表
     */
    @Override
    public List<Issue> getIssues(String url, String state, String version) {
        String apiURL = parseURL(url, state, version);
        log.info(apiURL);
        // 获取issue
        JSONArray issues = fetchAllRepositoryIssues(apiURL);
        // 按时间过滤
        issues = filterJSONObjectByTime(issues);
        log.info(String.valueOf(issues.size()));
        List<Issue> issueList = new ArrayList<>();
        List<Future<Issue>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < issues.size(); i++) {
            // issue 部分
            JSONObject issue = issues.getJSONObject(i);
            final Long issueId = issue.getLong("number");
            final String userName = issue.getJSONObject("user").getString("login");
            final IssueTitle title = generateIssueTitle(issue.getString("title"));
            final List<IssueBody> issueBodyList = generateIssueBody(issue.getString("body"));
            final Date createTime = parseDate(issue.getString("created_at"));
            // 获取issue对应comment
            Integer commentNum = issue.getInteger("comments");
            if (commentNum > 0) {
                final String commentsURL = issue.getString("comments_url");
                // 并发请求
                Future<Issue> future = executor.submit(() -> {
                    JSONArray comments = JSON.parseArray(makeApiRequest(commentsURL));
                    List<Comment> commentList = new ArrayList<>();
                    for (int j = 0; j < comments.size(); j++) {
                        JSONObject comment = comments.getJSONObject(j);
                        commentList.add(generateComment(comment, issueId));
                    }
                    return new Issue(issueId, userName, title, issueBodyList, createTime, commentList);
                });
                futures.add(future);
            } else {
                issueList.add(new Issue(issueId, userName, title, issueBodyList, createTime, new ArrayList<>()));
            }
        }

        for (Future<Issue> future : futures) {
            try {
                Issue issue = future.get();
                issueList.add(issue);
            } catch (ExecutionException | InterruptedException e) {
                throw new SentiException(13, "并发错误");
            }
        }

        executor.shutdown();

        return issueList;
    }

    @Override
    public Report getReport() {
        // 获取标注结果
        List<MarkResult> markResultList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MARK_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String aspect = line.substring(line.indexOf("【") + 1, line.indexOf("】"));
                markResultList.add(new MarkResult(aspect, line));
            }
        } catch (FileNotFoundException e) {
            throw new SentiException(14, "文件地址有误");
        } catch (IOException e) {
            throw new SentiException(15, "获取标注结果时发生IO异常");
        }
        // 读取报告内容
        try {
            Path filePath = Paths.get(REPORT_FILE); // 指定文件路径
            byte[] content = Files.readAllBytes(filePath);
            return new Report(markResultList, new String(content));
        } catch (IOException e) {
            throw new SentiException(16, "获取报告内容时发生IO异常");
        }
    }

    private JSONArray filterJSONObjectByTime(JSONArray issues) {
        if (timeByVersion[0] != null) {
            return IntStream.range(0, issues.size())
                    .mapToObj(issues::getJSONObject)
                    .filter(issue -> {
                        Date createTime = parseDate(issue.getString("created_at"));
                        return createTime.compareTo(parseDate(timeByVersion[0])) <= 0 && createTime.compareTo(parseDate(timeByVersion[1])) >= 0;
                    })
                    .collect(JSONArray::new, JSONArray::add, JSONArray::addAll);
        } else {
            return IntStream.range(0, issues.size())
                    .mapToObj(issues::getJSONObject)
                    .filter(issue -> {
                        Date createTime = parseDate(issue.getString("created_at"));
                        return createTime.compareTo(parseDate(timeByVersion[1])) >= 0;
                    })
                    .collect(JSONArray::new, JSONArray::add, JSONArray::addAll);
        }
    }

    private IssueTitle generateIssueTitle(String title) {
        return new IssueTitle(title, SentiUtil.calSentimentScores(title));
    }

    private List<IssueBody> generateIssueBody(String body) {
        List<IssueBody> issueBodyList = new ArrayList<>();
        if (body != null) {
            issueBodyList = Arrays.stream(preprocess(body).split("\\r\\n"))
                    .map(String::trim)
                    .filter(paragraph -> !paragraph.isEmpty() && !paragraph.matches("^\\s+$"))
                    .map(paragraph -> new IssueBody(paragraph, SentiUtil.calSentimentScores(paragraph)))
                    .collect(Collectors.toList());
        }
        return issueBodyList;
    }

    private Comment generateComment(JSONObject comment, Long issueId) {
        Long commentId = comment.getLong("id");
        String user = comment.getJSONObject("user").getString("login");
        Date createAt = parseDate(comment.getString("created_at"));
        String commentBody = preprocess(comment.getString("body")).replaceAll("\\r\\n", "");
        Integer commentScore = SentiUtil.calSentimentScores(commentBody);
        return new Comment(commentId, issueId, user, commentBody, commentScore, createAt);
    }

    /**
     * 对于 md 文本做一些预处理，去除图片、代码块、标签、多余的换行等
     * @param mdText
     * @return
     */
    private String preprocess(String mdText) {
        // 删除引用
        String processedText = mdText.replaceAll("> .*", "");
        // 删除图片 ![...](...)
        processedText = processedText.replaceAll("!\\[[^]]*]\\([^)]*\\)", "");
        // 删除代码块 ```...```
        processedText = processedText.replaceAll("```[\\s\\S]*?```", "");
        // 删除代码格式 `xxx`
        processedText = processedText.replaceAll("`([\\s\\S]*?)`", "$1");
        // 删除超链接 [xxx](...)
        processedText = processedText.replaceAll("\\[([^]]*)]\\([^)]*\\)", "$1");
        // 删除标签 <.../>, <xx>...<xx/>
        processedText = processedText.replaceAll("<[^>]*>", "");
        // 处理换行
        processedText = processedText.replaceAll("\r\n|\r|\n", "\r\n");
        // 处理连续换行
        processedText = processedText.replaceAll("(\r\n){2,}", "\r\n");
        return processedText;
    }


    private JSONArray fetchAllRepositoryIssues(String apiURL) {
        int perPage = 100; // 每页请求的数量，最大为 100
        int page = 1; // 当前页数
        JSONArray resultArray = new JSONArray();
        while (true) {
            String url = apiURL + "&per_page=" + perPage + "&page=" + page;
            String response = makeApiRequest(url);
            JSONArray responseArray = JSON.parseArray(response); // 将返回的 JSON 数组类型字符串转换为 JSON 数组对象
            resultArray.addAll(responseArray); // 将返回的 JSON 数据添加到结果中

            // 判断当前页是否还有更多数据，如果没有则退出循环
            if (responseArray.size() < perPage) {
                break;
            }
            page++; // 请求下一页数据
        }
        return resultArray;
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
     * @param version 仓库版本名
     * @return api请求的url
     */
    private String parseURL(String repositoryURL, String state, String version) {
        StringBuilder apiURL = new StringBuilder(API_BASE_URL);
        // 使用正则处理仓库URL, 获取所有者及仓库名
        Pattern pattern = Pattern.compile("https://github.com/(\\w+)/([\\w-]+)/?");
        Matcher matcher = pattern.matcher(repositoryURL);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            // 验证仓库有没有开启issue
            if (closeIssue(owner, repo)) {
                throw new SentiException(10, "该仓库没有开启issue功能");
            }
            // 获取两个大版本的时间
            timeByVersion = getTimeByVersion(owner, repo, version);
            // 拼接apiURL
            apiURL.append(owner).append('/').append(repo).append('/').append("issues");
            if ("all".equals(state) || "closed".equals(state)) {
                apiURL.append("?state=").append(state);
            } else {
                apiURL.append("?state=").append("open");
            }
            apiURL.append("&since=").append(timeByVersion[1]);
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
     * 检查仓库是否关闭issue功能
     * @param owner 仓库拥有者
     * @param repo 仓库名
     * @return 是否开启issue功能
     */
    private boolean closeIssue(String owner, String repo) {
        JSONObject repoInfo = JSON.parseObject(makeApiRequest(API_BASE_URL + owner + '/' + repo));
        return !repoInfo.getBoolean("has_issues");
    }


    /**
     * 获取两个相邻版本的时间, 其中第二个版本即所要查询的版本
     * @param owner
     * @param repo
     * @param version
     * @return
     */
    private String[] getTimeByVersion(String owner, String repo, String version) {
        String[] versionTime = new String[2];
        JSONArray releasesInfo = JSON.parseArray(makeApiRequest(API_BASE_URL + owner + '/' + repo + "/releases"));
        if (!releasesInfo.isEmpty()) {
            // 对数组中的json对象按发布时间排序
            List<JSONObject> jsonList = new ArrayList<>();
            for (int i = 0; i < releasesInfo.size(); i++) {
                jsonList.add(releasesInfo.getJSONObject(i));
            }
            jsonList.sort((o1, o2) -> {
                Date date1 = parseDate(o1.getString("published_at"));
                Date date2 = parseDate(o2.getString("published_at"));
                // 比较时间
                return date2.compareTo(date1);
            });
            // 查询时间
            for (int i = 0; i < jsonList.size(); i++) {
                JSONObject release = jsonList.get(i);
                String releaseName = release.getString("name");
                if (version.equals(releaseName)) {
                    versionTime[0] = i == 0 ? null : releasesInfo.getJSONObject(i - 1).getString("published_at");
                    versionTime[1] = release.getString("published_at");
                    return versionTime;
                }
            }
            throw new SentiException(11, "仓库不包括该版本");
        } else {
            throw new SentiException(12, "该仓库暂无release");
        }

    }
}
