#!groovy
pipeline {
    agent any
    environment {
        DEVOPS_WORKSPACE = "pulumi-automation"
        PULUMI_STACK = "pulumi-aws-simple"
    }
    parameters{
        string(defaultValue: '<replace with jenkins cred hash>',description: 'AWS credentials', name: 'AWS_CREDENTIAL')
        string(defaultValue: '<replace with aws region>',description: 'AWS Region', name: 'AWS_REGION')
        password(defaultValue: '<replace with pulumi access token>',description: 'Pulumi Access token', name: 'PULUMI_ACCESS_TOKEN')

        
    }
    options {
        // Only keep the 5 most recent builds
        buildDiscarder(logRotator(numToKeepStr: '5'))
        skipDefaultCheckout(true)
    }
    stages {

        stage('Pulumi : Login'){
            steps{
                dir(DEVOPS_WORKSPACE){
                    timestamps{
                        script{
                            sh "export PULUMI_ACCESS_TOKEN=${PULUMI_ACCESS_TOKEN}; pulumi login"
                        }
                    }
                }
            }
        }

        stage('Pulumi : Initialize stack'){
            steps{
                dir(DEVOPS_WORKSPACE){
                            timestamps {
                                script {
                                    sh """
                                        #!/bin/bash
                                        stack=\$(pulumi stack ls | grep "pulumi-aws-simple" | awk {'print \$1'})
                                        if [ -z \$stack  ]; then
                                            pulumi stack init \$PULUMI_STACK
                                        else
                                            pulumi stack select \$PULUMI_STACK
                                        fi
                                        pulumi config set aws:region \$AWS_REGION
                                        """
                                }
                            }  
                    }
                }

            }

        stage('Pulumi : Deployment'){
            steps{
                dir(DEVOPS_WORKSPACE){
                     withCredentials([
                            [$class: 'UsernamePasswordMultiBinding', credentialsId: "${params.AWS_CREDENTIAL}", usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY']
                        ])
                        {
                            timestamps{
                                script{
                                    sh """    
                                        #!/bin/bash
                                        echo "==> Cleanup Python temp files."
                                        rm -rf venv Pipfile.lock __pycache__
                                                                               
                                        python3 -m venv venv
                                        . venv/bin/activate
                                        set AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
                                        set AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}

                                        echo "==> Install Python modules."                                
                                        pip3 install -r requirements.txt;
                                        echo "==> Pulumi deployment."
                                        pulumi up -y
                                     
                                        deactivate
                                    """
                                }
                            }
                        }
                }
            }
        }

        stage('Pulumi : Cleanup'){
            steps{
                dir(DEVOPS_WORKSPACE){
                     withCredentials([
                            [$class: 'UsernamePasswordMultiBinding', credentialsId: "${params.AWS_CREDENTIAL}", usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY']
                        ])
                        {
                            timestamps{
                                script{
                                    sh """  
                                        #!/bin/bash
                                        echo "==> Destroy AWS EC2 instances."
                                        python3 -m venv venv
                                        . venv/bin/activate
                                        set AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
                                        set AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}

                                        pulumi destroy -y


                                        pulumi stack rm ${PULUMI_STACK} -y

                                        deactivate
                                        rm -rf venv Pipfile.lock __pycache__
                                    """
                            }
                        }
                    }

                }
            }

        }

        stage('Pulumi : Logout'){
            steps{
                dir(DEVOPS_WORKSPACE){
                    timestamps{
                        script{
                            sh "pulumi logout"
                        }
                    }
                }
            }
        }
    }
}