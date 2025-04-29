pipeline {
    agent any

    environment {
        DOCKER = 'docker'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
                echo '✅ SCM 클론 성공'
            }
        }

        // .env 파일 준비 + SSH 키 가져오기
        stage('Prepare Environment') {
            steps {
                withCredentials([
                    file(credentialsId: 'carenest-env', variable: 'ENV_FILE'),
                    string(credentialsId: 'ssh-private-key', variable: 'SSH_PRIVATE_KEY')
                ]) {
                    sh """
                        cp \$ENV_FILE ${WORKSPACE}/.env
                        echo 'SSH_PRIVATE_KEY="${SSH_PRIVATE_KEY}"' >> ${WORKSPACE}/.env
                    """
                    echo '✅ .env 파일 준비 완료'
                }
            }
        }

        stage('Build eureka-service image') {
            steps {
                dir('eureka-service') {
                    sh '''
                        chmod +x ./gradlew
                        ./gradlew clean build
                        ${DOCKER} build -t jongmin627/eureka-service .
                    '''
                }
                echo '✅ eureka-service Docker Image 빌드 완료'
            }
        }

        stage('Build config-service image') {
            steps {
                dir('config-service') {
                    sh '''
                        chmod +x ./gradlew
                        ./gradlew clean build
                        ${DOCKER} build -t jongmin627/config-service .
                    '''
                }
                echo '✅ config-service Docker Image 빌드 완료'
            }
        }

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
                echo '🧹 이전 컨테이너 제거 완료'
            }
        }

        stage('Run config-service') {
            steps {
                sh """
                    ${DOCKER} run --name config-service -d -p 8888:8888 \
                    --env-file ${WORKSPACE}/.env \
                    -e SSH_PRIVATE_KEY="$SSH_PRIVATE_KEY" \
                    jongmin627/config-service
                """
                echo '🚀 config-service 컨테이너 실행 완료'
            }
        }

        stage('Run eureka-service') {
            steps {
                sh '''
                    ${DOCKER} run --name eureka-service -d -p 8761:8761 \
                    jongmin627/eureka-service
                '''
                echo '🚀 eureka-service 컨테이너 실행 완료'
            }
        }
    }
}