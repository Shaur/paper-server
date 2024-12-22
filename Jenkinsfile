pipeline {
  agent any
  options {
    skipStagesAfterUnstable()
    skipDefaultCheckout()
  }
  environment { ... }
  stages {
    stage("Prepare container") {
      agent {
        docker {
          image 'openjdk:24-jdk-slim'
        }
      }
      stages {
        stage('Build') {
         checkout scm
         sh './gradlew build'
        }
      }
    }
  }
}