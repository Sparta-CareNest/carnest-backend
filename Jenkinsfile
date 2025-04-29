pipeline {
    agent any

    environment {
        DOCKER = 'docker'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
                echo 'Checkout SCM 성공'
            }
        }

        // 1. Prepare .env file (Clone 후 바로)
        stage('Prepare .env file') {
            steps {
                withCredentials([file(credentialsId: 'carenest-env', variable: 'ENV_FILE')]) {
                    sh '''
                        cp $ENV_FILE ${WORKSPACE}/.env
                    '''
                    echo '.env 파일 준비 완료 (Jenkins Credentials에서 가져옴)'
                }
            }
        }

        // 2. eureka-service Build
        stage('Build eureka-service image') {
            steps {
                dir('eureka-service') {
                    sh '''
                        ls -al
                        chmod +x ./gradlew
                        ./gradlew clean build
                        ${DOCKER} build -t jongmin627/eureka-service .
                    '''
                }
                echo 'eureka-service Docker Image 빌드 완료'
            }
        }

        // 3. config-service Build
        stage('Build config-service image') {
            steps {
                dir('config-service') {
                    sh '''
                        ls -al
                        chmod +x ./gradlew
                        ./gradlew clean build
                        ${DOCKER} build -t jongmin627/config-service .
                    '''
                }
                echo 'config-service Docker Image 빌드 완료'
            }
        }

        // 4. Remove old containers
        stage('Remove Previous containers') {
            steps {
                script {
                    sh '''
                        ${DOCKER} stop eureka-service || true
                        ${DOCKER} rm eureka-service || true
                        ${DOCKER} stop config-service || true
                        ${DOCKER} rm config-service || true
                    '''
                }
                echo '이전 컨테이너 제거 완료 (없는 경우 무시)'
            }
        }

        // 5. Run New config-service first
        stage('Run New config-service container') {
            steps {
                sh """
                    ${DOCKER} run --name config-service -d -p 8888:8888 \
                    --env-file ${WORKSPACE}/.env \
                    jongmin627/config-service
                """
                echo '새로운 config-service 컨테이너 실행 완료'
            }
        }

        // 6. Run New eureka-service
        stage('Run New eureka-service container') {
            steps {
                sh '''
                    ${DOCKER} run --name eureka-service -d -p 8761:8761 \
                    jongmin627/eureka-service
                '''
                echo '새로운 eureka-service 컨테이너 실행 완료'
            }
        }
    }
}