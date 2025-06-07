pipeline {
    agent {
        kubernetes {
            cloud 'kubernetes'
            namespace 'jenkins'
            defaultContainer 'jnlp'
            yaml """
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: m.daocloud.io/docker.io/jenkins/inbound-agent:latest
                    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
            """
        }
    }
    stages {
        stage('print') {
            steps {
                echo 'hello'
            }
        }

    }
}