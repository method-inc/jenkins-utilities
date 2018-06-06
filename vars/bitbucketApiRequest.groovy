#!/usr/bin/env groovy

def call(Map config) {
  // Required arguments
  String path = config.path
  // Optional arguments
  String method = config.getOrDefault('method', 'GET')
  Map body = config.getOrDefault('body', null)

  String bitbucketRootUrl = gitUrl().contains('bitbucket.org') ?
    'https://api.bitbucket.org' :
    env.BITBUCKET_API_ROOT
  String credentialID = env.BITBUCKET_API_CREDENTIAL ?
    env.BITBUCKET_API_CREDENTIAL :
    'bitbucket-svc-readonly-api'

  def apiResponse
  withCredentials([usernameColonPassword(credentialsId: credentialID, variable: 'USERPASS')]) {
    apiResponse = httpRequest url: "${bitbucketRootUrl}/${path}",
                              httpMode: method,
                              requestBody: groovy.json.JsonOutput.toJson(body),
                              customHeaders: [
                                [
                                  maskValue: true,
                                  name: 'Authorization',
                                  value: "Basic ${USERPASS.bytes.encodeBase64().toString()}"
                                ],
                                [
                                  name: 'Content-Type',
                                  value: 'application/json'
                                ]
                              ]
  }
  return apiResponse
}
