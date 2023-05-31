package com.mickey.sentibackend.controller;

import com.mickey.sentibackend.entity.Issue;
import com.mickey.sentibackend.entity.Report;
import com.mickey.sentibackend.entity.Result;
import com.mickey.sentibackend.service.IssueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@CrossOrigin
public class IssueController {

    private final IssueService issueService;

    @Autowired
    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/get-issues")
    public Result<List<Issue>> getIssues(@RequestParam("url") String url,
                                         @RequestParam("state") String state,
                                         @RequestParam("version") String version) {
        return Result.buildSuccess(issueService.getIssues(url, state, version));
    }

    @GetMapping("/report")
    public Result<Report> getMarkdownContent() {
        return Result.buildSuccess(issueService.getReport());
    }
}
