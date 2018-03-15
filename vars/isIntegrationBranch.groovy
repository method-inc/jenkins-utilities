#!/usr/bin/env groovy

boolean call() {
  return env.BRANCH_NAME ==~ /^(master|develop|release\/[\S]*)$/
}
