pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_PATH = './docker-compose.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo '✅ 코드 클론 완료'
            }
        }

        stage('Build config-server JAR') {
            steps {
                sh '''
                    chmod +x ./gradlew
                    ./gradlew :config-server:clean :config-server:build -x test
                '''
            }
        }

        stage('Build eureka-server JAR') {
            steps {
                sh '''
                    ./gradlew :eureka-server:clean :eureka-server:build -x test
                '''
            }
        }

        stage('Build gateway-server JAR') {
            steps {
                sh '''
                    ./gradlew :gateway-server:clean :gateway-server:build -x test
                '''
            }
        }

        stage('Docker Compose Deploy') {
            steps {
                withCredentials([
                    file(credentialsId: 'env-file-secret', variable: 'ENV_FILE'),
                    string(credentialsId: 'ssh-private-key', variable: 'SSH_PRIVATE_KEY')
                ]) {
                    sh '''
                        cp $ENV_FILE .env
                        printf "%b" "$SSH_PRIVATE_KEY" > id_rsa
                        chmod 600 id_rsa

                        echo "🛠️ Docker Compose로 서비스 전체 배포 중..."
                        docker compose -f ${DOCKER_COMPOSE_PATH} down || true
                        docker compose -f ${DOCKER_COMPOSE_PATH} up -d --build
                    '''
                }
                echo '🚀 config, eureka, gateway 서버 Docker Compose로 배포 완료'
            }
        }
    }
}