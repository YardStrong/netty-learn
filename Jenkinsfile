pipeline {
    agent {
        kubernetes {
            cloud 'devops-k8s'
            namespace 'devops-jenkins'
            agentInjection false
            containerTemplate(
                    name: 'jnlp',
                    image: 'jenkins/inbound-agent:3309.v27b_9314fd1a_4-1',
                    args: '${computer.jnlpmac} ${computer.name}'
            )
            containerTemplate(
                    name: 'maven',
                    image: 'maven:3.8.6-jdk-8',
                    ttyEnabled: true,
                    command: 'cat',
                    envVars: [
                            envVar(
                                    key: 'MAVEN_OPTS',
                                    value: '-Dmaven.repo.local=/usr/share/maven/ref/repository -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true'
                            )
                    ],
                    configMapVolume mountPath: '/root/.m2/', name: 'jenkins-maven-settings',
                    resourceRequestCpu: '500m',
                    resourceLimitCpu: '2000m',
                    resourceRequestMemory: '1Gi',
                    resourceLimitMemory: '4Gi'
            )
            retries 1
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