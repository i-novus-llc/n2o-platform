pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        git url: 'https://git.i-novus.ru/platform/n2o.git', branch: 'master'
      }
    }

    stage('Maven build') {
      steps {
        def rtMaven = Artifactory.newMavenBuild()
        // Tool name from Jenkins configuration
        rtMaven.tool = "default"
        // Set Artifactory repositories for dependencies resolution and artifacts deployment.
        rtMaven.deployer releaseRepo:'libs-release-local', snapshotRepo:'libs-snapshot-local', server: Artifactory.server "jfrog"
        rtMaven.resolver releaseRepo:'libs-release', snapshotRepo:'libs-snapshot', server: Artifactory.server "jfrog"
        def buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean install'
        server.publishBuildInfo buildInfo
      }
    }
  }
}
