// pipeline {
//     agent any
//
//     environment {
//         DOCKER = 'docker'
//     }
//
//     stages {
//         stage('Clone Repository') {
//             steps {
//                 checkout scm
//                 echo '✅ SCM 클론 성공'
//             }
//         }
//
//         stage('Build eureka-service image') {
//             steps {
//                 dir('eureka-service') {
//                     sh '''
//                         chmod +x ./gradlew
//                         ./gradlew clean build
//                         ${DOCKER} build -t jongmin627/eureka-service .
//                     '''
//                 }
//                 echo '✅ eureka-service Docker Image 빌드 완료'
//             }
//         }
//
//         stage('Build config-service image') {
//             steps {
//                 dir('config-service') {
//                     sh '''
//                         chmod +x ./gradlew
//                         ./gradlew clean build
//                         ${DOCKER} build -t jongmin627/config-service .
//                     '''
//                 }
//                 echo '✅ config-service Docker Image 빌드 완료'
//             }
//         }
//
//         stage('Remove Previous containers') {
//             steps {
//                 sh '''
//                     ${DOCKER} stop config-service || true
//                     ${DOCKER} rm config-service || true
//                     ${DOCKER} stop eureka-service || true
//                     ${DOCKER} rm eureka-service || true
//                 '''
//                 echo '🧹 이전 컨테이너 제거 완료'
//             }
//         }
//
//         stage('Prepare .env and Run config-service') {
//             steps {
//                 withCredentials([
//                     string(credentialsId: 'git-uri', variable: 'GIT_URI'),
//                     string(credentialsId: 'git-paths', variable: 'GIT_SEARCH_PATHS'),
//                     string(credentialsId: 'git-label', variable: 'GIT_DEFAULT_LABEL'),
//                     file(credentialsId: 'ssh-private-key', variable: 'SSH_KEY_FILE'),
//                     string(credentialsId: 'ssh-host-key', variable: 'SSH_HOST_KEY'),
//                     string(credentialsId: 'ssh-algorithm', variable: 'SSH_HOST_KEY_ALGORITHM'),
//                     string(credentialsId: 'ssh-passphrase', variable: 'SSH_PASSPHRASE')
//                 ]) {
//                     sh '''
//                         echo "GIT_URI=$GIT_URI" > ${WORKSPACE}/.env
//                         echo "GIT_SEARCH_PATHS=$GIT_SEARCH_PATHS" >> ${WORKSPACE}/.env
//                         echo "GIT_DEFAULT_LABEL=$GIT_DEFAULT_LABEL" >> ${WORKSPACE}/.env
//                         echo "GIT_IGNORE_LOCAL_SSH_SETTINGS=true" >> ${WORKSPACE}/.env
//                         echo "SSH_HOST_KEY=$SSH_HOST_KEY" >> ${WORKSPACE}/.env
//                         echo "SSH_HOST_KEY_ALGORITHM=$SSH_HOST_KEY_ALGORITHM" >> ${WORKSPACE}/.env
//                         echo "SSH_PASSPHRASE=$SSH_PASSPHRASE" >> ${WORKSPACE}/.env
//                         echo -n "SSH_PRIVATE_KEY=" >> ${WORKSPACE}/.env
//                         cat $SSH_KEY_FILE | sed ':a;N;$!ba;s/\\n/\\\\n/g' | tr '\n' '\\n' >> ${WORKSPACE}/.env
//                     '''
//
//                     sh '''
//                         ${DOCKER} run --name config-service -d -p 8888:8888 \
//                         --env-file ${WORKSPACE}/.env \
//                         jongmin627/config-service
//                     '''
//                     echo '🚀 config-service 실행 완료'
//                 }
//             }
//         }
//
//         stage('Run eureka-service') {
//             steps {
//                 sh '''
//                     ${DOCKER} run --name eureka-service -d -p 8761:8761 \
//                     jongmin627/eureka-service
//                 '''
//                 echo '🚀 eureka-service 컨테이너 실행 완료'
//             }
//         }
//     }
// }