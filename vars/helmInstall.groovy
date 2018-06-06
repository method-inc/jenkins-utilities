#!/usr/bin/env groovy

def call(Map config) {
    List<String> arguments = config.getOrDefault('args', [])

    if (config.values) {
        arguments.add(0, "--values ${config.values}")
    }
    if (config.version) {
        arguments.add("--version ${config.version}")
    }
    if (config.namespace) {
        arguments.add("--namespace ${config.namespace}")
    } else if (env.OC_PROJECT) {
        arguments.add("--namespace ${env.OC_PROJECT}")
    }

    sh "helm upgrade --install ${config.name} ${config.chart} --wait --force ${arguments.join(' ')}"
}
