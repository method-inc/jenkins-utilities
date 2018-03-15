#!/usr/bin/env groovy

String call(str) {
  return str.replaceAll('[^a-zA-Z0-9]+', '-').toLowerCase()
}
