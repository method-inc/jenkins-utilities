#!/usr/bin/env groovy

// NOTE: Currently only supports Bitbucket Server

def call(Map config) {
  String path = config.path

  String method = config.getOrDefault('method', 'GET')
  Map body = config.getOrDefault('body', null)
  
  def apiResponse
  if (isPullRequest()) {
    apiResponse = bitbucketApiRequest path: "rest/api/1.0/projects/${gitRepoOwner()}/repos/${gitRepoName()}/pull-requests/${env.CHANGE_ID}/${path}",
                                      method: method,
                                      body: body
  } else {
    print 'WARNING: This is not a pull request; you cannot call the bitbucketPRApiRequest method.'
  }
  return apiResponse
}
