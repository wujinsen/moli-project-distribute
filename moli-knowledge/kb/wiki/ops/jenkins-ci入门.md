---
title: Jenkins CI 入门
slug: jenkins-ci入门
type: concept
status: active
tags: [Jenkins, CI, DevOps]
sources:
- raw/wujinsen_markdown/架构/DevOps/conflunce文档/Confluence7.4安装并破解汉化教程.note.md
- raw/wujinsen_markdown/架构/DevOps/conflunce文档/confluence破解版安装.note.md
- raw/wujinsen_markdown/架构/DevOps/conflunce文档/conflunce文档安装.note.md
- raw/wujinsen_markdown/架构/DevOps/gitlab/GitLab端口冲突 解决办法.note.md
- raw/wujinsen_markdown/架构/DevOps/gitlab/gitlab docker 安装.note.md
- raw/wujinsen_markdown/架构/DevOps/gitlab/gitlab external_url采坑记.note.md
- raw/wujinsen_markdown/架构/DevOps/gitlab/gitlab常用命令.note.md
- raw/wujinsen_markdown/架构/DevOps/gitlab/linux gitlab安装.note.md
- raw/wujinsen_markdown/架构/DevOps/gitlab/使用 GitLab + Jenkins 实现自动化构建.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/Jenkins安装.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins linux安装.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins pipeline 脚本.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins使用docker安装.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins各种配置.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins目录讲解.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins自动部署jar包（maven.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins集成sonarqube.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/pipeline 流水线脚本.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/系列教程/(一) Jenkins从0-1搭建--jenkins部署.note.md
- raw/wujinsen_markdown/架构/DevOps/jenkins/系列教程/jenkins各种问题总结.note.md
- raw/wujinsen_markdown/架构/DevOps/jira/JIRA 7.8 版本的安装与破解.note.md
- raw/wujinsen_markdown/架构/DevOps/jira/JIRA支持mysql版本问题.note.md
- raw/wujinsen_markdown/架构/DevOps/jira/atlassian-agent.jar 打印信息.note.md
- raw/wujinsen_markdown/架构/DevOps/jira/jira mac安装.note.md
- raw/wujinsen_markdown/架构/DevOps/jira/jira8.19安装教程, 支持mysql8.note.md
- raw/wujinsen_markdown/架构/DevOps/nexus/maven---nexus私服配置setting和pom.note.md
- raw/wujinsen_markdown/架构/DevOps/nexus/nexus私服搭建.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/Linux 普通用户启动nginx.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/Nginx 安装.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/Redis+Nginx实现高并发缓存架构.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/moli nginxpei.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/moli nginx配置文件.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/nginx安装2.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/nginx配置二级目录，反向代理不同ip+端口.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/nginx配置信息.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/nginx配置文件.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/使用nginx部署多个前端项目.note.md
- raw/wujinsen_markdown/架构/DevOps/nginx/配置文件/nginx gateway路由信息.note.md
- raw/wujinsen_markdown/架构/DevOps/sonar/sonar-sonarscanner.note.md
- raw/wujinsen_markdown/架构/DevOps/sonar/sonarqube7.1 linux安装.note.md
- raw/wujinsen_markdown/架构/DevOps/sonar/sonarqube7.6 linux安装.note.md
- raw/wujinsen_markdown/架构/DevOps/sonar/sonarqube安装.note.md
- raw/wujinsen_markdown/架构/DevOps/组件配置文档/组件配置文档.note.md
related: [linux-运维基础, k8s入门与容器编排]
created: 2026-06-22
updated: 2026-07-05
---

# Jenkins CI 入门

**CI**：代码提交后自动构建、测试、打包，减少「在我机器能跑」问题。可选用 Jenkins 对父 POM 与各 `*-server` 模块做流水线。

## 1. 核心概念

| 概念 | 说明 |
|------|------|
| Job / Pipeline | 一次构建流水线（Declarative/Jenkinsfile） |
| Agent | 执行节点（Linux + JDK8 + Maven） |
| Stage | 阶段：Checkout → Build → Test → Deploy |
| Artifact | 产出 jar/war |

## 2. 典型 Pipeline（Maven 多模块）

```groovy
pipeline {
 agent any
 stages {
 stage('Checkout') { steps { checkout scm } }
 stage('Build') {
 steps {
 sh 'cd moli-distribute-parent && mvn clean install -DskipTests'
 sh 'cd moli-user-center && mvn clean package -DskipTests'
 }
 }
 stage('Archive') {
 steps { archiveArtifacts '**/target/*.jar' }
 }
 }
}
```

按需并行编译 order/bi/knowledge/gateway。

## 3. 部署方式

| 方式 | 说明 |
|------|------|
| SSH 传 jar + restart | 传统单机，见 [[ops/linux-运维基础]] |
| Docker 镜像 | build 镜像 push 仓库，见 |
| K8s | 更新 Deployment 镜像，见 [[ops/k8s入门与容器编排]] |

## 4. 安装要点

- Linux：`java -jar jenkins.war` 或 Docker 官方镜像。
- 插件：Git、Pipeline、Maven Integration、Credentials。
- 与 SonarQube 集成可选（代码质量门禁）。

## 相关

 · `moli-knowledge/kb/wiki-moli/ops/nginx反向代理与前端部署指南.md`
## 批次#1310 增补（wujinsen P0）

合并 `架构/DevOps/` Jenkins 安装、Pipeline、自动部署 jar 等 raw。

## 批次#1320 增补（wujinsen Phase2 P0）

补挂 `架构/DevOps/jenkins/` Maven 构建 raw。

原文插图 annex：[[ops/annex-nexus私服搭建]]
