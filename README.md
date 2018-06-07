# Jenkins Utilities

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
