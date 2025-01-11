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
                sh 'docker build -t paper-service .'
                sh 'docker tag paper-service paper.webhop.me/paper-service:latest'
                sh 'docker push paper.webhop.me/paper-service:latest'
            }
        }
        stage('Helm deploy') {
            steps {
                withKubeConfig([credentialsId: 'kubernetes-creds', serverUrl: "https://192.268.3.229:16443", namespace: "default"]) {
                    sh 'helm --help'
                }
            }
        }
      }
    }
  }
}