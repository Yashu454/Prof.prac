pipeline {
    agent any

    environment {
        MAVEN_HOME = tool 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out the code from the repository.'
                checkout scm
            }
        }
        stage('Build') {
            steps {
                echo 'Building the code using Maven.'
                sh "${MAVEN_HOME}/bin/mvn clean package"
            }
        }
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests.'
                sh "${MAVEN_HOME}/bin/mvn test"
            }
        }
        stage('Static Code Analysis') {
            steps {
                echo 'Performing static code analysis with SonarQube.'
                sh 'sonar-scanner'
            }
        }
        stage('Security Check') {
            steps {
                echo 'Running security checks using OWASP Dependency-Check.'
                sh 'mvn dependency-check:check'
            }
        }
        stage('Staging Deployment') {
            steps {
                echo 'Deploying the application to the staging environment.'
                sh 'ansible-playbook deploy-staging.yml'
            }
        }
        stage('Acceptance Tests') {
            steps {
                echo 'Running acceptance tests on the staging environment.'
                sh 'mvn verify'
            }
        }
        stage('Production Deployment') {
            steps {
                echo 'Deploying the application to the production environment.'
                sh 'ansible-playbook deploy-production.yml'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        success {
            echo 'Sending success email notification.'
            emailext (
                to: 'dondayaswanthsai@gmail.com',
                subject: 'Pipeline Status: SUCCESS',
                body: 'The pipeline completed successfully. Build artifacts are attached.',
                attachLog: true
            )
        }
        failure {
            echo 'Sending failure email notification.'
            emailext (
                to: 'dondayaswanthsai@gmail.com',
                subject: 'Pipeline Status: FAILURE',
                body: 'The pipeline failed. Please check the build log for details.',
                attachLog: true
            )
        }
    }
}
