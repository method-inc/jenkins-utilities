#!/usr/bin/env groovy

// NOTE: Currently only supports Bitbucket Server

def call(Map config) {
  bitbucketApiRequest path: "rest/build-status/1.0/commits/${env.GIT_COMMIT}",
                      method: 'POST',
                      body: config
}
