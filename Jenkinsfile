pipeline {
    agent any
    
    environment {
   //change this to your sdk path
        ANDROID_HOME = 'C:\\Users\\Kmsmr\\AppData\\Local\\Android\\Sdk'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Kokorody/Budget_Tracker_Wallawet.git',
                        credentialsId: 'github-credentials-id'
                    ]]
                ])
            }
        }
        
        stage('Clean') {
            steps {
                bat './gradlew clean'
            }
        }

        stage('Build') {
            steps {
                bat './gradlew assembleDebug'
            }
        }

        stage('Test') {
            steps {
                bat './gradlew test'
            }
        }

        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: '**/app/build/outputs/apk/debug/app-debug.apk',
                    allowEmptyArchive: false
            }
        }
    }

    post {
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}