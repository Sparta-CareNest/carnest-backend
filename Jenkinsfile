pipeline {
    agent any

    environment {
        DOCKER = 'sudo docker'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Build CareNest') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean build
                '''
            }
        }

        stage('Build and Deploy Eureka-Service') {
            steps {
                dir('eureka-service') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew build
                        ${DOCKER} build -t jongmin627/eureka-service .
                        ${DOCKER} stop eureka-service || true
                        ${DOCKER} rm eureka-service || true
                        ${DOCKER} run --name eureka-service -d -p 8761:8761 jongmin627/eureka-service
                    '''
                }
            }
        }

        stage('Build and Deploy Gateway-Service') {
            steps {
                dir('gateway-service') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew build
                        ${DOCKER} build -t jongmin627/gateway-service .
                        ${DOCKER} stop gateway-service || true
                        ${DOCKER} rm gateway-service || true
                        ${DOCKER} run --name gateway-service -d -p 8080:8080 jongmin627/gateway-service
                    '''
                }
            }
        }

        stage('Build and Deploy Config-Service') {
            steps {
                dir('config-service') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew build
                        ${DOCKER} build -t jongmin627/config-service .
                        ${DOCKER} stop config-service || true
                        ${DOCKER} rm config-service || true
                        ${DOCKER} run --name config-service -d -p 8888:8888 jongmin627/config-service
                    '''
                }
            }
        }
    }
}