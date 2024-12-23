pipeline {
  agent {
      label "kubeagent"
  }
  options {
    skipStagesAfterUnstable()
    skipDefaultCheckout()
  }
  stages {
    stage("Prepare container") {
      stages {
        stage('Build') {
        steps {
            sh 'apk update'
            sh 'apk add git'
            checkout scm
            sh 'chmod +x gradlew'
            sh './gradlew build -x test'
         }
        }
      }
    }
  }
}