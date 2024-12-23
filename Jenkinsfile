pipeline {
  parameters {
    string(defaultValue: 'Spaces-1', description: 'ID of the Octopus Space', name: 'SpaceId', trim: true)
    string(defaultValue: 'Budget_Tracker_Wallawet', description: 'Name of the project in Octopus Deploy', name: 'ProjectName', trim: true)
    string(defaultValue: 'Dev', description: 'Target environment for deployment', name: 'EnvironmentName', trim: true)
    string(defaultValue: 'Octopus', description: 'ID of the Octopus Server', name: 'ServerId', trim: true)
  }
  agent any
  stages {
    stage('Validate Parameters') {
      steps {
        script {
          if (params.ProjectName.trim() == '' || params.SpaceId.trim() == '' || params.EnvironmentName.trim() == '' || params.ServerId.trim() == '') {
            error("One or more required parameters are missing. Please check the input values.")
          }
        }
      }
    }
    stage('Environment Info') {
      steps {
        echo "Environment Variables:"
        echo "PATH = ${env.PATH}"
        echo "Jenkins Workspace: ${env.WORKSPACE}"
      }
    }
    stage('Checkout Code') {
      steps {
        script {
          retry(3) { // Coba ulang hingga 3 kali
            checkout([
              $class: 'GitSCM',
              branches: [[name: '*/main']], // Ubah 'main' jika branch berbeda
              userRemoteConfigs: [[
                url: 'https://github.com/Kokorody/Budget_Tracker_Wallawet.git',
                credentialsId: 'your-credentials-id' // Ganti dengan ID kredensial Anda
              ]]
            ])
          }
        }
      }
    }
    stage('Package Application') {
      steps {
        // Run GitVersion if available
        sh(script: 'which gitversion && gitversion /output buildserver || true')
        script {
          if (fileExists('gitversion.properties')) {
            def props = readProperties file: 'gitversion.properties'
            env.VERSION_SEMVER = props.GitVersion_SemVer
          } else {
            env.VERSION_SEMVER = "1.0.0." + env.BUILD_NUMBER
          }
          def packageName = 'Budget_Tracker_Wallawet'
          env.ARTIFACT_NAME = "${packageName}.${env.VERSION_SEMVER}.zip"
          octopusPack(
            sourcePath: '.', 
            outputPath: '.', 
            includePaths: "**/*", 
            packageFormat: 'zip', 
            packageId: packageName, 
            packageVersion: env.VERSION_SEMVER, 
            overwriteExisting: true
          )
        }
      }
    }
    stage('Deploy Application') {
      steps {
        script {
          def packagePaths = env.ARTIFACT_NAME
          octopusPushPackage(
            packagePaths: packagePaths, 
            overwriteMode: 'OverwriteExisting', 
            serverId: params.ServerId, 
            spaceId: params.SpaceId
          )
          octopusCreateRelease(
            project: params.ProjectName, 
            releaseVersion: env.VERSION_SEMVER, 
            environment: params.EnvironmentName, 
            serverId: params.ServerId, 
            spaceId: params.SpaceId
          )
          octopusDeployRelease(
            project: params.ProjectName, 
            releaseVersion: env.VERSION_SEMVER, 
            environment: params.EnvironmentName, 
            serverId: params.ServerId, 
            spaceId: params.SpaceId, 
            waitForDeployment: true
          )
        }
      }
    }
  }
  post {
    success {
      echo "Pipeline executed successfully!"
    }
    failure {
      echo "Pipeline execution failed."
    }
  }
}
