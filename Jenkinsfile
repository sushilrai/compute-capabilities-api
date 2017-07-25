UPSTREAM_JOBS_LIST = [
    "vce-symphony/common-dependencies/${env.BRANCH_NAME}",
    "vce-symphony/common-messaging-parent/${env.BRANCH_NAME}"
]
UPSTREAM_JOBS = UPSTREAM_JOBS_LIST.join(',')

pipeline {    
    triggers {
        upstream(upstreamProjects: UPSTREAM_JOBS, threshold: hudson.model.Result.SUCCESS)
    }
    agent {
        node {
            label 'maven-builder'
            customWorkspace "workspace/${env.JOB_NAME}"
        }
    }
    environment {
        GITHUB_TOKEN = credentials('github-02')
    }
    options { 
        skipDefaultCheckout()
        buildDiscarder(logRotator(artifactDaysToKeepStr: '30', artifactNumToKeepStr: '5', daysToKeepStr: '30', numToKeepStr: '5'))
        timestamps()
        disableConcurrentBuilds()
    }
    tools {
        maven 'linux-maven-3.3.9'
        jdk 'linux-jdk1.8.0_102'
    }
    stages {
        stage('Checkout') {
            steps {
                doCheckout()
	    }
	}
        stage('Compile') {
            steps {
                sh "mvn install -Dmaven.repo.local=.repo -DskipTests=true -DskipITs=true"
            }
        }
        stage('Unit Testing') {
            steps {
                sh "mvn test -Dmaven.repo.local=.repo"
            }
        }
        stage('Record Test Results') {
            steps {
                junit '**/target/*-reports/*.xml'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    if (env.BRANCH_NAME ==~ /stable.*/) {
                        withCredentials([string(credentialsId: 'GPG-Dell-Key', variable: 'GPG_PASSPHRASE')]) {
                            sh "mvn deploy -Dmaven.repo.local=.repo -DskipTests=true -DskipITs=true -Ppublish-release -Dgpg.passphrase=${GPG_PASSPHRASE} -Dgpg.keyname=73BD7C5F -DskipJavadoc=false -DskipJavasource=false"
                        }
                    } else {
                        sh "mvn deploy -Dmaven.repo.local=.repo -DskipTests=true -DskipITs=true"
                    }
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
		doSonarAnalysis()
            }
        }
        stage('Third Party Audit') {
            steps {
		doThirdPartyAudit()
            }
        }
	stage('PasswordScan') {
            steps {
		doPwScan()
            }
	}
        stage('Github Release') {
            steps {
                githubRelease()
	    }
        }
        stage('NexB Scan') {
            steps {
                sh 'rm -rf .repo'
                doNexbScanning()
            }
        }
    }
    post {
        always {
            cleanWorkspace()
        }
        success {
            successEmail()
        }
        failure {
            failureEmail()
        }
    }
}
