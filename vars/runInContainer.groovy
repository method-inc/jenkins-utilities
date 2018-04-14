#!/usr/bin/env groovy

def call(Map config, Closure body) {
  String cloud = config.getOrDefault('cloud', 'kubernetes')
  String project = config.getOrDefault('project', 'openshift')
  String tag = config.getOrDefault('tag', 'latest')

  boolean isJenkinsAgent = config.getOrDefault('isJenkinsAgent', false)
  String jenkinsAgentArgs = config.getOrDefault('jenkinsAgentArgs', '${computer.jnlpmac} ${computer.name}')

  // If `image` is provided, `project` and `imagestream` are ignored.
  String image = config.getOrDefault('image',
    "docker-registry.default.svc:5000/${project}/${config.imagestream}"
  )

  String podLabel = urlSafeString("${env.JOB_NAME}-${env.BUILD_ID}")
  String testContainerName = 'test-container'

  def containers = []
  if (isJenkinsAgent) {
    containers.push(containerTemplate(
      name: 'jnlp',
      image: "${image}:${tag}",
      args: jenkinsAgentArgs,
      alwaysPullImage: true
    ))
  } else {
    containers.push(containerTemplate(
      name: testContainerName,
      image: "${image}:${tag}",
      ttyEnabled: true,
      command: 'cat',
      alwaysPullImage: true
    ))
  }

  podTemplate(label: podLabel, cloud: cloud, containers: containers) {
    node(podLabel) {
      if (isJenkinsAgent) {
        body()
      } else {
        container(testContainerName) {
          body()
        }
      }
    }
  }
}
