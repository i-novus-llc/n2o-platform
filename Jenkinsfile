pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        git(url: 'https://git.i-novus.ru/platform/n2o.git', branch: 'master', changelog: true, poll: true)
      }
    }
  }
}