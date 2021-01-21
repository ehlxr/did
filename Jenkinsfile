pipeline {
    agent any

    environment {
        DOCKER_REGISTRY_USER = credentials('docker-registry-user')
        DOCKER_REGISTRY_PWD = credentials('docker-registry-pwd')
        DINGTALK_TOKEN = credentials('dingtalk-token')
        DOCKER_IMAGE_TAG = createVersion()
    }

    parameters {
        gitParameter(branch: '',
                    branchFilter: '.*',
                    defaultValue: 'origin/master',
                    listSize: '10',
                    name: 'GIT_BRANCH',
                    quickFilterEnabled: true,
                    selectedValue: 'DEFAULT',
                    sortMode: 'DESCENDING_SMART',
                    tagFilter: '*',
                    type: 'PT_BRANCH_TAG',
					description: 'Select your branch or tag.')
        choice(name: 'MODULE_PARMS', choices: ['did-server'], description: 'select module of project')
        string(name: 'DOCKER_IMAGE_TAG', defaultValue: '', description: 'docker 镜像 tag，默认为当前时间戳 + 编译号码，例如：20190909_113757_89')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: "${params.GIT_BRANCH}"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [],
                    submoduleCfg: [],
                    userRemoteConfigs: [
                        [
                            credentialsId: 'git_credential',
                            url: 'https://git.ehlxr.me/ehlxr/did.git'
                        ]
                    ]
                ])
            }
        }

        stage('Build') {
            steps {
                sh "mvn clean install -DskipTests -e -U"
                sh "cp ${MODULE_PARMS}/target/${MODULE_PARMS}*.jar && sh build.sh"
            }
        }
    }

    post {
        success {
            sh sendMsg("Jenkins Pipeline Finished: SUCCESS! \n docker image tag is: ${env.DOCKER_IMAGE_TAG}")
        }
        failure {
            sh sendMsg("Jenkins Pipeline Finished: FAILED!")
        }
    }

}

def createVersion() {
    if("${env.DOCKER_IMAGE_TAG}" == ""){
        return new Date().format('yyyyMMdd_HHmmss') + "_${env.BUILD_NUMBER}"
    }

    return "${env.DOCKER_IMAGE_TAG}"
}

def sendMsg(String msg) {
    return "curl -s https://oapi.dingtalk.com/robot/send?access_token=${env.DINGTALK_TOKEN} \
                -H 'Content-Type: application/json' \
                -d '{\"msgtype\": \"text\",\"text\": {\"content\": \""+ msg +"\"}}'"
}