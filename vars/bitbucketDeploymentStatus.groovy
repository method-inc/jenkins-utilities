#!/usr/bin/env groovy

// NOTE: Currently only supports Bitbucket Server

def call(Map config) {
  String SUCCESSFUL_STATE = 'SUCCESSFUL'

  // Required Arguments
  String url = config.url
  // Optional Arguments
  String state = config.getOrDefault('state', SUCCESSFUL_STATE)

  bitbucketCommitStatus state: state,
                        key: "DEPLOYMENT-${urlSafeBranchName()}",
                        name: "Deploy » ${env.BRANCH_NAME}",
                        url: url,
                        description: "Deployed to ${url}"

  if (state == SUCCESSFUL_STATE && isPullRequest()) {
    bitbucketPRComment text: "This pull request has been deployed to ${url}."
  }
}
