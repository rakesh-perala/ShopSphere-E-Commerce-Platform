pipeline {

    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    options {
        disableConcurrentBuilds()
        timestamps()

        buildDiscarder(
            logRotator(
                numToKeepStr: '20'
            )
        )
    }

    environment {

        SERVICE_NAME = 'user-service'

        SERVICE_DIR = 'application/services/user-service'

        DOCKER_IMAGE = 'dockerperala/shopsphere-user-service'

        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {

        stage('Git Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Resolve Application Version') {
            steps {
                dir("${SERVICE_DIR}") {

                    script {

                        env.APP_VERSION = sh(
                            script: '''
                                mvn help:evaluate \
                                  -Dexpression=project.version \
                                  -q \
                                  -DforceStdout
                            ''',
                            returnStdout: true
                        ).trim()

                        echo "Service       : ${SERVICE_NAME}"
                        echo "Application Version : ${APP_VERSION}"
                    }
                }
            }
        }

        stage('Maven Build & Test') {
            steps {
                dir("${SERVICE_DIR}") {

                    sh '''
                        mvn clean test
                    '''
                }
            }

            post {
                always {
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {

                dir("${SERVICE_DIR}") {

                    withSonarQubeEnv('SonarQube') {

                        sh '''
                            ${SCANNER_HOME}/bin/sonar-scanner \
                              -Dsonar.projectName=shopsphere-user-service \
                              -Dsonar.projectKey=shopsphere-user-service \
                              -Dsonar.sources=src/main/java \
                              -Dsonar.java.binaries=target/classes
                        '''
                    }
                }
            }
        }

        stage('SonarQube Quality Gate') {

            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate(
                        abortPipeline: true
                    )
                }
            }
        }

        stage('Trivy Filesystem Scan') {

            steps {

                dir("${SERVICE_DIR}") {

                    sh '''
                        trivy fs \
                          --scanners vuln,secret,misconfig \
                          --severity HIGH,CRITICAL \
                          --exit-code 1 \
                          --no-progress \
                          .
                    '''
                }
            }
        }

        stage('Identify Build Artifact') {

            steps {

                dir("${SERVICE_DIR}") {

                    script {

                        env.JAR_FILE = sh(
                            script: '''
                                find target \
                                  -maxdepth 1 \
                                  -type f \
                                  -name "*.jar" \
                                  ! -name "*.jar.original" \
                                  | head -n 1
                            ''',
                            returnStdout: true
                        ).trim()

                        if (!env.JAR_FILE) {
                            error "JAR artifact not found"
                        }

                        echo "Build artifact: ${env.JAR_FILE}"
                    }
                }
            }
        }

        stage('Upload to Nexus') {

            when {
                expression {
                    return env.BRANCH_NAME?.startsWith('release/')
                }
            }

            steps {

                echo "Nexus upload will be configured for release builds."
            }
        }

        stage('Docker Build') {

            when {
                expression {
                    return env.BRANCH_NAME?.startsWith('release/')
                }
            }

            steps {

                dir("${SERVICE_DIR}") {

                    sh '''
                        docker build \
                          -t ${DOCKER_IMAGE}:${APP_VERSION} \
                          .
                    '''
                }
            }
        }

        stage('Trivy Docker Image Scan') {

            when {
                expression {
                    return env.BRANCH_NAME?.startsWith('release/')
                }
            }

            steps {

                sh '''
                    trivy image \
                      --severity HIGH,CRITICAL \
                      --no-progress \
                      ${DOCKER_IMAGE}:${APP_VERSION}
                '''
            }
        }

        stage('Docker Hub Push') {

            when {
                expression {
                    return env.BRANCH_NAME?.startsWith('release/')
                }
            }

            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-cred',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    sh '''
                        echo "$DOCKER_PASSWORD" | \
                          docker login \
                          -u "$DOCKER_USERNAME" \
                          --password-stdin

                        docker push \
                          ${DOCKER_IMAGE}:${APP_VERSION}

                        docker logout
                    '''
                }
            }
        }
    }

    post {

        success {
            echo "ShopSphere ${SERVICE_NAME} CI pipeline completed successfully."
        }

        failure {
            echo "ShopSphere ${SERVICE_NAME} CI pipeline failed."
        }

        always {
            archiveArtifacts(
                artifacts: "${SERVICE_DIR}/target/*.jar",
                allowEmptyArchive: true
            )
        }
    }
}
