#!/usr/bin/env groovy

def call(Map config, Closure body) {
  String cloud = config.getOrDefault('cloud', 'kubernetes')
  String dockerImage = config.getOrDefault('dockerImage', 'docker')
  String dockerTag = config.getOrDefault('dockerTag', '18.04')
  String dindImage = config.getOrDefault('dindImage', 'docker')
  String dindTag = config.getOrDefault('dindTag', '18.04-dind')
  String dockerCPU = config.getOrDefault('dockerCPU', '100m')
  String dockerMem = config.getOrDefault('dockerMem', '256Mi')
  String daemonCPU = config.getOrDefault('daemonCPU', '100m')
  String daemonMem = config.getOrDefault('daemonMem', '256Mi')

  String podLabel = 'docker-in-docker'
  String dockerCommandsContainerName = 'docker-cmds'

  podTemplate (
    label: podLabel,
    cloud: cloud,
    containers: [
      containerTemplate(
        name: dockerCommandsContainerName,
        image: "${dockerImage}:${dockerTag}",
        ttyEnabled: true,
        command: 'docker run -p 80:80 httpd:latest',
        envVars: [ envVar(key: 'DOCKER_HOST', value: 'tcp://localhost:2375') ],
        resourceRequestCpu: dockerCPU,
        resourceLimitCpu: dockerCPU,
        resourceRequestMemory: dockerMem,
        resourceLimitMemory: dockerMem
      ),
      containerTemplate(
        name: 'dind-daemon',
        image: "${dindImage}:${dindTag}",
        ttyEnabled: true,
        privileged: true,
        resourceRequestCpu: daemonCPU,
        resourceLimitCpu: daemonCPU,
        resourceRequestMemory: daemonMem,
        resourceLimitMemory: daemonMem
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
