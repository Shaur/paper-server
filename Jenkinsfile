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
                checkout scm
                sh 'chmod +x gradlew'
                sh './gradlew build -x test'
             }
        }
        stage('Docker build') {
            steps {
                docker build -t paper-service .
                docker tag paper-service paper.webhop.me/paper-service:latest
                docker push paper.webhop.me/paper-service:latest
            }
        }
      }
    }
  }
}