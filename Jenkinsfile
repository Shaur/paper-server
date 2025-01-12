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
                sh "docker tag paper-service ${DOCKER_REGISTRY}/paper-service:latest"
                sh "docker push ${DOCKER_REGISTRY}/paper-service:latest"
            }
        }
        stage('Helm deploy') {
            steps {
                withKubeConfig([serverUrl: "${CLUSTER_URL}", namespace: "default"]) {
                    sh 'helm upgrade --install paper-service paper-chart'
                }
            }
        }
      }
    }
  }
}