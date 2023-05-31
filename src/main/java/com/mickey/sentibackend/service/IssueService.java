package com.mickey.sentibackend.service;

import com.mickey.sentibackend.entity.Issue;
import com.mickey.sentibackend.entity.Report;

import java.util.List;

public interface IssueService {
    List<Issue> getIssues(String url, String state, String version);

    Report getReport();
}
