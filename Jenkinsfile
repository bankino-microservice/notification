def dockerImage
pipeline{
    agent any
    tools{
        maven "Maven"
        jdk "Java"
    }
    environment{
        CLUSTER_NAME = "ebanking-dev-cluster"
        Scanner_home= tool 'sonar'
        CREDENTIAL_ID="adminuser:us-west-2:awstoken"
        appregistery="342547628532.dkr.ecr.us-west-2.amazonaws.com/ebanking"
        projectregistery="https://342547628532.dkr.ecr.us-west-2.amazonaws.com"


        // AWS Region
        AWS_REGION = "us-west-2"

        // Registry URL (No https:// prefix for shell commands)
        REGISTRY_URL = "342547628532.dkr.ecr.us-west-2.amazonaws.com"

        // Full Image Name
        IMAGE_NAME = "342547628532.dkr.ecr.us-west-2.amazonaws.com/ebanking"
        POSTGRES_HOST = '35.88.142.67'
        POSTGRES_PORT = '5432'
        POSTGRES_USER = 'ebank'
        POSTGRES_PASSWORD = 'ebank'
        POSTGRES_DB = 'ebankdb'
        POSTGRES_SSL = 'false'
    }
    parameters{
        string(name:'ecr_name',defaultValue:'ebank')
    }
    stages{
        stage("Checkout"){
            steps{
              git branch:"jenkins-acc" , url:"https://github.com/bankino-microservice/Account-service.git",
                credentialsId: 'git-token'


            }
        }







         stage("Build with Maven") {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage("SonarQube Analysis"){
            steps{
                withSonarQubeEnv("SonarServer") {
                    sh "${Scanner_home}/bin/sonar-scanner -Dsonar.projectName=ebank -Dsonar.projectKey=ebank -Dsonar.sources=src -Dsonar.java.binaries=target/classes"
                }
            }
        }

        stage("Quality Gate"){
            steps{
                waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
            }
        }

        stage("Trivy Filesystem Scan"){
            steps{
                sh 'trivy fs . --exit-code 0 > trivy-result.txt || true'
                archiveArtifacts artifacts: 'trivy-result.txt', onlyIfSuccessful: true
            }
        }

    stage("Build & Push to ECR") {
        steps {
            script {
                withCredentials([
                    aws(
                        credentialsId: "${CREDENTIAL_ID}",
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    )
                ]) {
                    sh """
                    # 1. Login to ECR
                    aws ecr get-login-password --region ${AWS_REGION} \
                    | docker login --username AWS --password-stdin ${REGISTRY_URL}

                    # 2. Build Docker Image (immutable tag)
                    docker build -t ${IMAGE_NAME}:account${BUILD_NUMBER} .

                    # 3. Tag as legacy-latest (mutable tag)
                    docker tag ${IMAGE_NAME}:account${BUILD_NUMBER} ${IMAGE_NAME}:account-latest

                    # 4. Push both tags
                    docker push ${IMAGE_NAME}:account${BUILD_NUMBER}
                    docker push ${IMAGE_NAME}:account-latest
                    """
                }
            }
        }
    }


  stage("Deploy to EKS") {
              steps {
                  script {
                      withCredentials([aws(credentialsId: "${CREDENTIAL_ID}", accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                          sh """
                          # 1. Login to EKS
                          aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}



                          # 3. Apply the updated file
                          kubectl apply -f k8s/compte.yaml


                          """
                      }
                  }
              }
          }

        stage("delete image"){
            steps{
              sh 'docker rmi -f $(docker images -a -q)'
            }
        }
    }
  
}