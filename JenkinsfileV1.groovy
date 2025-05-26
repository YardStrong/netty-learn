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
                    resourceRequestCpu: '500m',
                    resourceLimitCpu: '2000m',
                    resourceRequestMemory: '1Gi',
                    resourceLimitMemory: '4Gi'
            )
            retries 1
        }
    }
    stages {
        stage('Clone repository') {
            steps {
                checkout scmGit(branches: [[name: '*/main']],
                        extensions: [cloneOption(depth: 1, noTags: true, reference: '', shallow: true)],
                        userRemoteConfigs: [[credentialsId: 'git_rsa', url: 'git@gitee.com:YardStrong/netty-learn.git']]
                )
            }
        }

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