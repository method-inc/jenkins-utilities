#!/usr/bin/env groovy

def call(Map config, Closure body) {
  String cloud = config.getOrDefault('cloud', 'kubernetes')
  String dockerImage = config.getOrDefault('dockerImage', 'docker')
  String dockerTag = config.getOrDefault('dockerTag', '18.04')
  String dindImage = config.getOrDefault('dindImage', 'docker')
  String dindTag = config.getOrDefault('dindTag', '18.04-dind')

  String podLabel = 'docker-in-docker'
  String dockerCommandsContainerName = 'docker-cmds'

  podTemplate (
    label: podLabel,
    cloud: cloud,
    containers: containers: [
      containerTemplate(
        name: dockerCommandsContainerName,
        image: "${dockerImage}:${dockerTag}",
        ttyEnabled: true,
        command: 'docker run -p 80:80 httpd:latest',
        envVars: [ envVar(key: 'DOCKER_HOST', value: 'tcp://localhost:2375') ],
        resourceRequestCpu: '10m',
        resourceLimitCpu: '100m',
        resourceRequestMemory: '256Mi',
        resourceLimitMemory: '512Mi'
      ),
      containerTemplate(
        name: 'dind-daemon',
        image: "${dindImage}:${dindTag}",
        ttyEnabled: true,
        privileged: true,
        resourceRequestCpu: '20m',
        resourceLimitCpu: '100m',
        resourceRequestMemory: '512Mi',
        resourceLimitMemory: '512Mi'
      )
    ],
    volumes: [ emptyDirVolume(mountPath: '/var/lib/docker', memory: false) ]
  ) {
    node(podLabel) {
      container(dockerCommandsContainerName) {
        body()
      }
    }
  }
}
