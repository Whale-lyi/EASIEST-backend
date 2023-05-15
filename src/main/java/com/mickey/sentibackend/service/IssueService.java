package com.mickey.sentibackend.service;

import com.mickey.sentibackend.entity.Issue;

import java.util.List;

public interface IssueService {
    List<Issue> getIssues(String url, String state);
}
