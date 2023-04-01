pipeline {
  agent any
  environment {
    APP_NAME = 'microservices-demo'
    DOCKER_REGISTRY = 'docker.io'
    DOCKER_NAMESPACE = 'my-namespace'
    DOCKER_IMAGE_TAG = "${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${APP_NAME}:${env.BUILD_NUMBER}"
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean package'
        sh 'docker build -t $DOCKER_IMAGE_TAG .'
      }
    }
    stage('Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
          sh 'docker login $DOCKER_REGISTRY -u $DOCKER_USERNAME -p $DOCKER_PASSWORD'
          sh 'docker push $DOCKER_IMAGE_TAG'
        }
      }
    }
    stage('Deploy') {
      steps {
        sh 'kubectl apply -f kubernetes/deployment.yaml'
        sh 'kubectl rollout status deployment/$APP_NAME'
      }
    }
  }
}
