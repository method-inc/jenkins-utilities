#!/usr/bin/env groovy

def call() {
  def gitUrlStrings = gitUrl().split('/')
  return gitUrlStrings[gitUrlStrings.length - 1].split('.git')[0]
}
