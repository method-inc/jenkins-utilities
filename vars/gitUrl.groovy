#!/usr/bin/env groovy

def call() {
  // On branch builds GIT_URL will be present, but on pull
  // request builds only GIT_URL_1 and GIT_URL_2 are available.
  return env.GIT_URL ? env.GIT_URL : env.GIT_URL_1
}
