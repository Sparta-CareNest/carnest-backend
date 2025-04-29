pipeline {
    agent any

    environment {
        DOCKER = 'sudo docker'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
                echo 'Checkout SCM 성공'
            }
        }

        stage('Build image') {
            steps {
                dir('eureka-service') {
                    sh '''
                        ls -al
                        chmod +x ./gradlew
                        ./gradlew clean build
                        ${DOCKER} build -t jongmin627/eureka-service .
                    '''
                }
                echo 'Build Docker Image 성공'
            }
        }

        stage('Remove Previous container') {
            steps {
                script {
                    sh """
                        ${DOCKER} stop eureka-service || true
                        ${DOCKER} rm eureka-service || true
                    """
                }
                echo '이전 컨테이너 제거 완료 (없는 경우 무시)'
            }
        }

        stage('Run New container') {
            steps {
                sh """
                    ${DOCKER} run --name eureka-service -d -p 8761:8761 jongmin627/eureka-service
                """
                echo '새로운 Eureka-Service 컨테이너 실행 성공'
            }
        }
    }
}