#!/usr/bin/env groovy

boolean call() {
  return env.CHANGE_TARGET != null
}
