pipeline {
    agent {
        kubernetes {
            cloud 'kubernetes'
            namespace 'jenkins'
            agentInjection false
            yaml """
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: docker.1ms.run/jenkins/inbound-agent:latest
                    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
                  - name: maven
                    image: docker.1ms.run/maven:3.8.2-openjdk-8
                    command: ['sh', '-c']
                    args:
                    - cat
                    tty: true
            """
        }
    }
    stages {
        stage('Run compile') {
            steps {
                container('maven') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Run build') {
            steps {
                container('maven') {
                    sh 'mvn package -Dmaven.test.skip=true'
                }
            }
        }

        stage('Archive artifacts') {
            steps {
                archiveArtifacts(artifacts: '*/target/*.jar', followSymlinks: false)
            }
        }

    }
}