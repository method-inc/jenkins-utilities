# Jenkins Utilities

[![Build Status](https://travis-ci.org/Skookum/jenkins-utilities.svg?branch=master)](https://travis-ci.org/Skookum/jenkins-utilities)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

This repository is a [shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/) of utilities to be used in Jenkins pipelines.

The global functions contained in this repository should be considered alpha; future breaking changes may be expected.

## Usage

Import the library at the [latest released version tag](https://github.com/Skookum/jenkins-utilities/releases) and then call utility functions as steps in your declarative or scripted pipeline.

```groovy
// Jenkinsfile
@Library('jenkins-utilities@v1.0.0') _

pipeline {
    agent { docker 'maven:3-alpine' } 
    stages {
        stage('Example Build') {
            steps {
                cancelPreviousBuilds()
                sh 'mvn -B clean verify'
            }
        }
    }
}
```
