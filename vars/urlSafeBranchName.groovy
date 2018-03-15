#!/usr/bin/env groovy

String call() {
  return urlSafeString(env.BRANCH_NAME)
}
