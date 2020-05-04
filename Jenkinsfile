#!/usr/bin/env groovy

@Library("Common-pipeline") _
//This Common code is location here
//if you touch this code it effect every jenkins job
//github URL/Common-pipeline.git

env.BASE_NODE=''
env.BRANCHNAME="develop"
env.GIT_BROWSER="Source code repo URL"
env.GIT_BROWSER_VERSION='8.6'
env.GIT_SSH_URL="Source code repo URL.git"
env.JDK_VERS='OpenJDK 1.8 '
env.MVN_SETTINGS=''
env.MVN_VERS='maven30 SCL'
env.RELEASE_VERSION="1.0.0"
env.TAG_JOB=false

timeout(60)
{
	timestamps
	{
		node(BASE_NODE)
		{
			BASE_NODE = env.BASE_NODE
			BRANCHNAME = env.BRANCHNAME
			GIT_BROWSER = env.GIT_BROWSER
			GIT_BROWSER_VERSION = env.GIT_BROWSER_VERSION
			GIT_SSH_URL = env.GIT_SSH_URL
			JDK_VERS = env.JDK_VERS
			MVN_SETTINGS = env.MVN_SETTINGS
			MVN_VERS = env.MVN_VERS
			TAG_JOB = env.TAG_JOB
			RELEASE_VERSION = env.RELEASE_VERSION

			PostBuild("BASE_NODE " + BASE_NODE, "blue")
			PostBuild("BRANCHNAME " + BRANCHNAME, "blue")
			PostBuild("GIT_BROWSER " + GIT_BROWSER, "blue")
			PostBuild("GIT_SSH_URL " + GIT_SSH_URL, "blue")
			PostBuild("JDK_VERS " + JDK_VERS, "blue")
			PostBuild("MVN_SETTINGS " + MVN_SETTINGS, "blue")
			PostBuild("MVN_VERS " + MVN_VERS, "blue")
			PostBuild("RELEASE_VERSION " + RELEASE_VERSION, "blue")
			PostBuild("TAG_JOB " + TAG_JOB, "blue")

			MavenJobInputSettings("", BASE_NODE, BRANCHNAME, GIT_BROWSER, GIT_BROWSER_VERSION, GIT_SSH_URL, JDK_VERS, MVN_SETTINGS, MVN_VERS, RELEASE_VERSION, TAG_JOB)

			MavenJobInputSettings("", BASE_NODE, BRANCHNAME, GIT_BROWSER, GIT_BROWSER_VERSION, GIT_SSH_URL, JDK_VERS, MVN_SETTINGS, MVN_VERS, RELEASE_VERSION, TAG_JOB)

			try
			{
				stage ('Checkout')
				{
					try
					{
						println('************************* Checkout *************************')

						cleanWs()
						BeginJobSetting()

						SCM_VARS = checkout([$class: 'GitSCM',
							branches: [[name: "*/${BRANCHNAME}"]],
							browser: [$class: 'GitLab', repoUrl: GIT_BROWSER, version: GIT_BROWSER_VERSION],
							extensions: [
								[$class: 'LocalBranch', localBranch: BRANCHNAME],
							],
							userRemoteConfigs: [[credentialsId: 'jenkins gitlab', url: GIT_SSH_URL]]
						])

					}
					catch(err)
					{
						GetError(err, "FAILURE", '************************* Checkout FAILED *************************')
						return
					}
				}
				stage ('Build')
				{
					try
					{
						DoMavenRelease(RELEASE_VERSION, JDK_VERS, MVN_VERS, MVN_SETTINGS, TAG_JOB.toString(), "false")
					}
					catch(err)
					{
						GetError(err, "FAILURE", '************************* Maven Build FAILED OverAll *************************')
						return
					}
				}
				stage ("Mail")
				{
					try
					{
						// Artifact Archiver
						//Save no jars
						//archiveArtifacts allowEmptyArchive: false, artifacts: 'Project/target/*.jar', caseSensitive: true, defaultExcludes: true, fingerprint: false, onlyIfSuccessful: false

						// Mailer notification
						yourmail()
					}
					catch(err)
					{
						GetError(err, "FAILURE", '************************* Mail Results FAILED *************************')
						return
					}
				}

			}
			catch(err)
			{
				GetError(err, "FAILURE", '************************* ${JOB_NAME} - FAILED *************************')
				return
			}
		}
	}
}
