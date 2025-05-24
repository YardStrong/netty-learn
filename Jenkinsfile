{
    containerTemplate(name: 'jnlp', image: 'registry01.yardstrong.online/docker.io/jenkins/inbound-agent', args: '${computer.jnlpmac} ${computer.name}'),
    pipeline{
        agent{
            label "node"
        }
        stages{
            stage("A"){
                steps{
                    echo "========executing A========"
                }
                post{
                    always{
                        echo "========always========"
                    }
                    success{
                        echo "========A executed successfully========"
                    }
                    failure{
                        echo "========A execution failed========"
                    }
                }
            }
        }
        post{
            always{
                echo "========always========"
            }
            success{
                echo "========pipeline executed successfully ========"
            }
            failure{
                echo "========pipeline execution failed========"
            }
        }
    }
}