pipeline {
    agent {
        label 'android-builder-node' // Ensure Jenkins node has required tools installed
    }
    environment {
        ANDROID_HOME = "/opt/android-sdk"
        PATH = "${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/Kokorody/Budget_Tracker_Wallawet.git', credentialsId: 'github-credentials-id'
            }
        }
        stage('Setup SDK') {
            steps {
                script {
                    sh '''
                    sdkmanager --licenses || true
                    sdkmanager "platform-tools" "build-tools;33.0.2" "platforms;android-33"
                    '''
                }
            }
        }
        stage('Build') {
            steps {
                sh './gradlew assembleDebug'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: '**/app/build/outputs/apk/debug/app-debug.apk', allowEmptyArchive: false
            }
        }
    }
    post {
        success {
            echo 'Build completed successfully.'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
