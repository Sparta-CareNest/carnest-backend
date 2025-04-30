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

        stage('Prepare Env & SSH Key') {
            steps {
                withCredentials([
                    file(credentialsId: 'env-file-secret', variable: 'ENV_FILE'),
                    string(credentialsId: 'ssh-private-key', variable: 'SSH_PRIVATE_KEY')
                ]) {
                    sh '''
                        cp $ENV_FILE .env
                        printf "%b" "$SSH_PRIVATE_KEY" > id_rsa
                        chmod 600 id_rsa
                    '''
                }
            }
        }

        stage('Docker Compose Up') {
            steps {
                sh '''
                    echo "🛠️ Docker Compose로 서비스 전체 배포 중..."
                    docker compose --env-file .env down || true
                    docker compose --env-file .env up -d --build --remove-orphans
                '''
                echo '🚀 전체 서비스 Docker Compose로 배포 완료'
            }
        }
    }
}