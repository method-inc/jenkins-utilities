#!/usr/bin/env groovy

// NOTE: Currently only supports Bitbucket Server

def call(Map config) {
  bitbucketPRApiRequest path: 'comments', method: 'POST', body: config
}
