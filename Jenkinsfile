pipeline {
    agent any

    environment {
        DOCKER = 'docker'
        NETWORK = 'msa-net'
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
                echo '✅ SCM 클론 성공'
            }
        }

        stage('Build config-service image') {
            steps {
                dir('config-service') {
                    sh '''
                        chmod +x ./gradlew
                        ./gradlew clean build --build-cache
                        ${DOCKER} build -t jongmin627/config-service .
                    '''
                }
                echo '✅ config-service Docker Image 빌드 성공'
            }
        }

        stage('Create Docker Network if Not Exists') {
            steps {
                sh """
                    if [ -z "\$(${DOCKER} network ls --filter name=^${NETWORK}$ --format='{{ .Name }}')" ]; then
                        echo "📡 Docker network '${NETWORK}' 생성 중..."
                        ${DOCKER} network create ${NETWORK}
                    else
                        echo "✅ Docker network '${NETWORK}' 이미 존재함"
                    fi
                """
            }
        }

        stage('Prepare .env and Launch config-service') {
            steps {
                withCredentials([
                    file(credentialsId: 'env-file-secret', variable: 'ENV_FILE'),
                    string(credentialsId: 'ssh-private-key', variable: 'SSH_PRIVATE_KEY')
                ]) {
                    sh '''
                        cp $ENV_FILE ${WORKSPACE}/.env
                        printf "%b" "$SSH_PRIVATE_KEY" > ${WORKSPACE}/id_rsa
                        chmod 600 ${WORKSPACE}/id_rsa

                        ${DOCKER} stop config-service || true
                        ${DOCKER} rm config-service || true

                        ${DOCKER} run -d \
                            --name config-service \
                            --network ${NETWORK} \
                            --env-file ${WORKSPACE}/.env \
                            -p 8888:8888 \
                            -v ${WORKSPACE}/id_rsa:/root/.ssh/id_rsa:ro \
                            jongmin627/config-service
                    '''
                    echo '🚀 config-service 실행 완료'
                }
            }
        }
    }
}