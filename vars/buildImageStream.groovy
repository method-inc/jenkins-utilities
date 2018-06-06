#!/usr/bin/env groovy

def deployToOpenshift(String valuesFile) {
  String DEPLOYMENT = deploymentName()

  sh "oc login https://kubernetes.default.svc:443 --insecure-skip-tls-verify --token ${OC_TOKEN}"

  // Set current namespace
  sh "oc project ${OC_PROJECT}"

  // Get credentials
  String serviceAccount = sh returnStdout: true, script: 'oc whoami | cut -d ":" -f 4'
  String pushSecret = sh returnStdout: true, script: "oc get -o jsonpath='{.imagePullSecrets[0].name}' sa ${serviceAccount}"

  if (isPublishBranch()) {
    helmInstall name: DEPLOYMENT,
                chart: 'base-app-build',
                version: '0.1.3',
                values: valuesFile,
                namespace: OC_PROJECT,
                args: [
                  "--set output.imagestream.enabled=false",
                  "--set output.docker.enabled=true",
                  "--set output.docker.repository=docker-registry.default.svc:5000/${imageStreamNamespace()}/${imageStreamName()}",
                  "--set output.docker.tag=${imageStreamTag()}",
                  "--set output.pushSecret=${pushSecret}",
                ]
  } else {
    helmInstall name: DEPLOYMENT,
                chart: 'base-app-build',
                version: '0.1.3',
                values: valuesFile,
                namespace: OC_PROJECT,
                args: [
                  "--set output.imagestream.namespace=${imageStreamNamespace()}",
                  "--set output.imagestream.name=${imageStreamName()}",
                  "--set output.imagestream.tag=${imageStreamTag()}"
                ]
  }

  sh "helm get ${DEPLOYMENT}"

  // Cancel any currently running builds
  //   This is necessary to clean up state from any Jenkins jobs triggered recently
  //   from the same branch or pull request.
  sh "oc cancel-build bc/${DEPLOYMENT}"

  // Trigger the buildconfig created above
  sh "oc start-build ${DEPLOYMENT} --from-dir=src --follow --token ${OC_TOKEN}"
}

def updateImagestreamTags() {
  String IMAGESTREAM_TAG = imageStreamTag()

  if (!isPublishBranch()) {
    echo "No imagestream tag updates needed for ${deploymentName()}"
    return
  }

  if (env.BRANCH_NAME == LATEST_BRANCH) {
    sh "oc tag ${PUBLISH_IMAGESTREAM}:${IMAGESTREAM_TAG} ${PUBLISH_IMAGESTREAM}:latest -n ${imageStreamNamespace()}"
  }
  if (env.BRANCH_NAME == STABLE_BRANCH) {
    sh "oc tag ${PUBLISH_IMAGESTREAM}:${IMAGESTREAM_TAG} ${PUBLISH_IMAGESTREAM}:stable -n ${imageStreamNamespace()}"
  }
}

def cleanupDeployment() {
  if (!isPublishBranch()) {
    // Remove temporary buildconfig and image tag
    sh "helm delete ${deploymentName()} --purge"
  }
}

boolean isPublishBranch() {
  return env.BRANCH_NAME == env.LATEST_BRANCH || env.BRANCH_NAME == env.STABLE_BRANCH
}

String imageStreamName() {
  return isPublishBranch() ? env.PUBLISH_IMAGESTREAM : "${env.PUBLISH_IMAGESTREAM}-${urlSafeBranchName()}"
}

String imageStreamNamespace() {
  return isPublishBranch() ? env.PUBLISH_PROJECT : env.OC_PROJECT
}

String imageStreamTag() {
  return isPublishBranch() ? "${urlSafeBranchName()}-canary" : "canary"
}

String deploymentName() {
  return isPublishBranch() ? "${imageStreamName()}-${urlSafeBranchName()}" : imageStreamName()
}

def call(Map config) {
  // Required arguments
  // Name of the ImageStream to be published
  String imageStreamArg   = config.imagestream
  // Pre-publish tests to run in built image
  Closure integrationTest = config.test

  // Optional arguments
  String agentLabelArg   = config.getOrDefault('agentLabel', 'default')
  // Name of the OpenShift project in which the ImageStream should be published
  String projectArg      = config.getOrDefault('project', 'openshift')
  // Name of the OpenShift project in which the BuildConfig should run
  String buildProjectArg = config.getOrDefault('buildNamespace', 'jenkins')
  // Name of the git branch that should be used to update the `latest` tag on the ImageStream
  String latestBranchArg = config.getOrDefault('latestBranch', 'master')
  // Name of the git branch that should be used to update the `stable` tag on the ImageStream
  String stableBranchArg = config.getOrDefault('stableBranch', 'master')
  // Path to the values overrides file for the base-app-build helm chart
  String valuesFile      = config.getOrDefault('valuesFile', './build-imagestream.yaml')
  // Directory containing test files which will be available in the resulting image during integration test stage
  String testDirArg      = config.getOrDefault('testDir', 'test')
  // Set to true if the ImageStream being published is meant to be used as a Jenkins agent
  boolean isJenkinsAgent = config.getOrDefault('isJenkinsAgent', false)
  // ID of Jenkins credential to auth to OpenShift
  String credentialIDArg = config.getOrDefault('credentialID', 'system:serviceaccount:jenkins:image-builder')

  pipeline {
    agent { label agentLabelArg }
    environment {
      OC_TOKEN=credentials("${credentialIDArg}")
      TILLER_NAMESPACE="${buildProjectArg}"
      OC_PROJECT="${buildProjectArg}"
      LATEST_BRANCH="${latestBranchArg}"
      STABLE_BRANCH="${stableBranchArg}"
      PUBLISH_PROJECT="${projectArg}"
      PUBLISH_IMAGESTREAM="${imageStreamArg}"
      TEST_DIR_EXISTS=fileExists(testDirArg)
    }
    stages {
      stage('Build') {
        steps {
          deployToOpenshift(valuesFile)
        }
      }
      stage('Stash Tests') {
        when {
          expression { return TEST_DIR_EXISTS }
        }
        steps {
          stash includes: "${testDirArg}/**/*", name: 'test'
        }
      }
      stage('Test') {
        when {
          expression { return integrationTest }
        }
        steps {
          // Run integration tests using the newly pushed image
          runInContainer(project: imageStreamNamespace(), imagestream: imageStreamName(), tag: imageStreamTag(), isJenkinsAgent: isJenkinsAgent) {
            script {
              if (TEST_DIR_EXISTS) {
                unstash 'test'
                dir('test') {
                  integrationTest()
                }
              } else {
                integrationTest()
              }
            }
          }
        }
      }
      stage('Publish') {
        steps {
          // Our unit and integration tests have passed, so it is now safe to 
          //   alias the "latest" and "stable" tag pointers to our newly pushed image
          updateImagestreamTags()
        }
      }
    }
    post {
      always {
        cleanupDeployment()
      }
    }
  }
}
