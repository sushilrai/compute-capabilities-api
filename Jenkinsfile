pipeline {    
    agent {
        node{
            label 'maven-builder'
            customWorkspace "workspace/${env.JOB_NAME}"
            }
    }
    environment {
        GITHUB_TOKEN = credentials('github-02')
    }
    options { 
        buildDiscarder(logRotator(artifactDaysToKeepStr: '30', artifactNumToKeepStr: '5', daysToKeepStr: '30', numToKeepStr: '5'))
        timestamps()
    }
    tools {
        maven 'linux-maven-3.3.9'
        jdk 'linux-jdk1.8.0_102'
    }
    stages {
        stage('Compile') {
            steps {
                sh "mvn install"
            }
        }
        stage('Unit Testing') {
            steps {
                sh "mvn test"
            }
        }
        stage('Deploy') {
            when {
                expression {
                    return env.BRANCH_NAME ==~ /develop|release\/.*/
                }
            }
            steps {
                sh "mvn deploy"
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') { 
                    sh '''
                            mvn sonar:sonar \
                                -Dsonar.host.url=$SONAR_HOST_URL \
                                -Dsonar.login=$SONAR_AUTH_TOKEN \
                                -Dsonar.java.coveragePlugin=jacoco \
                                -Dsonar.junit.reportsPath=target/surefire-reports \
                                -Dsonar.jacoco.reportPath=target/coverage-reports/jacoco-ut.exec \
                                -Dsonar.jacoco.itReportPath=target/coverage-reports/jacoco-it.exec \
                                -Dsonar.dependencyCheck.reportPath=${WORKSPACE}/report/dependency-check-report.xml
                        '''    
                }
            }
        }
        stage('Third Party Audit') {
            steps {
                sh '''
                    mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:analyze-report license:add-third-party org.apache.maven.plugins:maven-dependency-plugin:2.10:tree \
                    -DoutputType=dot \
                    -DoutputFile=${WORKSPACE}/report/dependency-tree.dot
                    '''   
                archiveArtifacts '**/dependency-analysis.html, **/THIRD-PARTY.txt, **/dependency-check-report.html, **/dependency-tree.dot'
            }
        }
        stage('Github Release') {
            when {
                expression {
                    return env.BRANCH_NAME ==~ /master|release\/.*/
                }
            }
            steps {
                dir('/opt'){
                    sh "rm -f linux-amd64-github-release.tar.bz2"
                    sh "wget https://github.com/aktau/github-release/releases/download/v0.7.2/linux-amd64-github-release.tar.bz2"
                    sh "tar -xvjf linux-amd64-github-release.tar.bz2"
                    sh "yes | cp bin/linux/amd64/github-release /usr/bin/"
                    sh '''
                        github-release release \
                            --user dellemc-symphony \
                            --repo  compute-capabilities-api \
                            --tag v0.0.1-${BRANCH_NAME}-${BUILD_ID} \
                            --name "compute-capabilities-api release" \
                            --description "compute-capabilities-api release"
                        github-release upload \
                            --user dellemc-symphony \
                            --repo compute-capabilities-api \
                            --tag v0.0.1-${BRANCH_NAME}-${BUILD_ID} \
                            --name "compute-capabilities-api release" \
                            --file ${WORKSPACE}/target/compute-capabilities-api-1.0-SNAPSHOT.jar
                    '''
                }
            }
        }
        stage('NexB Scan') {
            when {
                expression {
                    return env.BRANCH_NAME ==~ /master|develop|release\/.*/
                }
            }
            steps {
                sh "mvn clean"
                dir('/opt') {
                    checkout([$class: 'GitSCM', 
                              branches: [[name: '*/master']], 
                              doGenerateSubmoduleConfigurations: false, 
                              extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'nexB']], 
                              submoduleCfg: [], 
                              userRemoteConfigs: [[url: 'https://github.com/nexB/scancode-toolkit.git']]])
                }
				
		    sh "mkdir -p /opt/nexB/nexb-output/"
       
                    sh "sh /opt/nexB/scancode --format html ${WORKSPACE} /opt/nexB/nexb-output/compute-capabilities-api.html"
		    sh "sh /opt/nexB/scancode --format html-app ${WORKSPACE} /opt/nexB/nexb-output/compute-capabilities-api-grap.html"
	       
	            sh "mv /opt/nexB/nexb-output/ ${WORKSPACE}/"
	       	    archiveArtifacts '**/nexb-output/**'
            }
        }
    }
    post {
        always{
            step([$class: 'WsCleanup'])   
        }
        success {
            emailext attachLog: true, 
                body: 'Pipeline job ${JOB_NAME} success. Build URL: ${BUILD_URL}', 
                recipientProviders: [[$class: 'CulpritsRecipientProvider']], 
                subject: 'SUCCESS: Jenkins Job- ${JOB_NAME} Build No- ${BUILD_NUMBER}', 
                to: 'pebuildrelease@vce.com'            
        }
        failure {
            emailext attachLog: true, 
                body: 'Pipeline job ${JOB_NAME} failed. Build URL: ${BUILD_URL}', 
                recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider'], [$class: 'FailingTestSuspectsRecipientProvider'], [$class: 'UpstreamComitterRecipientProvider']], 
                subject: 'FAILED: Jenkins Job- ${JOB_NAME} Build No- ${BUILD_NUMBER}', 
                to: 'pebuildrelease@vce.com'
        }
    }
}
