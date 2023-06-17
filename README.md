  # EASIEST-backend

- 南京大学软件学院2023春《软件工程与计算Ⅲ》课程项目

- 本仓库为EASIEST工具的后端项目，通过EASIEST工具，用户可以爬取 GitHub 上仓库中某个 release 版本的 issue 及其相关信息，并通过 SentiStrength 工具进行分析，以图表等可视化方式展示出来。用户可以下载分析结果，手动对其进行 aspect 标注，从而分析版本问题

- 框架使用 SpringBoot 2.7.10, jdk 为 1.8

- 通过南大maven私服的方式引用了SentiStrength包, 请参看 pom.xml

- 通过GitLab Runner部署了完整的CICD流水线

- 后端通过不同端口部署了两套服务, 主分支一套, 其他分支一套
  - 详见流水线文件与 resources 目录下的不同配置文件
