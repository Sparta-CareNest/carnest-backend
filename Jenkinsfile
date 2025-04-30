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


        stage('Build Services') {
              steps {
                    sh '''
                        chmod +x ./gradlew
                        ./gradlew :config-service:build --build-cache -x test
                        ./gradlew :eureka-service:build --build-cache -x test
                        ./gradlew :gateway-service:build --build-cache -x test
                    '''
             }
        }

        stage('Create Docker Network if not exists') {
            steps {
                sh '''
                    if [ -z "$(docker network ls --filter name=^app-network$ --format '{{ .Name }}')" ]; then
                        echo "📡 Docker network 'app-network' 생성 중..."
                        docker network create app-network
                    else
                        echo "✅ Docker network 'app-network' 이미 존재함"
                    fi
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