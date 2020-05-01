#!/usr/bin/env groovy

import java.io.File
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
import hudson.AbortException
import hudson.model.AbstractBuild
import hudson.model.StringParameterValue
import hudson.model.ParametersAction
import groovy.io.FileType
import hudson.FilePath

def PostBuild(WhatJob, mycolor)
{
	println(WhatJob)
	def THEID = sh returnStdout: true, script: 'date +%s'
	addHtmlBadge html: "<font color = '" + mycolor + "'>" + WhatJob + "</font><br>", id: THEID
	manager.createSummary("orange-square.gif").appendText("<h2><font color = '" + mycolor + "'>" + WhatJob + "</font></h2>", false, false, false, "red")
}

def DoJIRA(THESTATE, Component_Name, TheFAILINGJOB, WHICHSECTION, STARTER_BUILD_NUMBER )
{
	println("================== START JIRA =======================")
	def TICKETNUMBER = ""
	def MakeJiraTicket = true
	if (binding.hasVariable(env.SPECIAL))
	{
		if ( env.SPECIAL.toBoolean() == true )
		{
			wrap([$class: 'BuildUser'])
			{
				def user = env.BUILD_USER_ID
					if ( user ==  "userID" )
					{
						println("Inside username: " + user)
						MakeJiraTicket = false
					}
			}
		}
	}
	else
	{
		env.SPECIAL = true
		MakeJiraTicket = true
	}
	println("MakeJiraTicket: " + MakeJiraTicket)
		if ( MakeJiraTicket == true )
		{
			stage("JIRA")
			{
				try
				{
					def propsFile = ""
					println("====== DEBUG 1 ======")
						if ( WHICHSECTION == "" )
							WHICHSECTION = "Project1"

						if ( WHICHSECTION == null )
							WHICHSECTION = "Project1"

						if ( TheFAILINGJOB == "" )
							TheFAILINGJOB = JOB_NAME

						if ( THESTATE.toString() == "FAILED")
						{
							if ( Component_Name == "Agent1")
								step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'userEmailID', sendToIndividuals: true])
							else if ( Component_Name == "Pack" )
								step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'userEmailID', sendToIndividuals: true])
							else if ( Component_Name == "Posix" )
								step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'userEmailID', sendToIndividuals: true])
							else if ( Component_Name == "IOS")
								step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'userGroupMailID', sendToIndividuals: true])
							else
								step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'userEmailID', sendToIndividuals: true])

							PostBuild( "Failing job is : " + TheFAILINGJOB , "red" )
							println("Email")
						}
					try
					{
						println("====== DEBUG 2 ======")
							if ( Component_Name == "Agent1" )
							{
								propsFile = readProperties file: WORKSPACE + "/Agent1/CREATETICKET.txt"
								AGENT_JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = AGENT_JIRA_PROJECT
							}
							else if ( Component_Name == "BoosterPack" )
							{
								propsFile = readProperties file: WORKSPACE + "/Pack/CREATETICKET.txt"
								BOOSTER_JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = PACK_JIRA_PROJECT
							}
							else if ( Component_Name == "Posix" )
							{
								propsFile = readProperties file: WORKSPACE + "/Posix/CREATETICKET.txt"
								POSIXSIM_JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = POSIX_JIRA_PROJECT
							}
							else if ( Component_Name == "Metrics")
							{
								propsFile = readProperties file: WORKSPACE + "/metric-definitions/CREATETICKET.txt"
								METRIC_JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = METRIC_JIRA_PROJECT
							}
							else if ( Component_Name == "Android")
							{
								propsFile = readProperties file: WORKSPACE + "/CREATETICKET.txt"
								OTA_JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = ANDROID_JIRA_PROJECT
							}
							else if ( Component_Name == "iOS")
							{
								propsFile = readProperties file: WORKSPACE + "/CREATETICKET.txt"
								JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = JIRA_PROJECT
							}
							else if ( Component_Name == "DeliveryPipeline")
							{
								propsFile = readProperties file: WORKSPACE + "/CREATETICKET.txt"
								OTA_JIRA_PROJECT = propsFile['CREATEDTICKETHERE']
								WHICHSECTION = DELIVERY_JIRA_PROJECT
							}
							else
							{
								WHICHSECTION = "PROJECT1"
							}
					}
					catch(err)
					{
						println("DOJIRA error " + err)
						currentBuild.result = 'SUCCESS'
						WHICHSECTION = "PROJECT1"
					}
					println("====== DEBUG 3 ======")

						if ( WHICHSECTION == null )
							WHICHSECTION = "PROJECT1"

					println("New Jira Ticket will be in " + WHICHSECTION)

					def ThisFoldername = TheFAILINGJOB.replace("/","/job/")
					def BUILD_URL = JENKINS_URL + "job/" + ThisFoldername + "/" + BUILD_NUMBER + "/"
					def CHANGE_URL = BUILD_URL + "changes"

					println("THESTATE: " + THESTATE )
					println("JOB_NAME: " + TheFAILINGJOB )
					println("BUILD_NUMBER: " + BUILD_NUMBER )
					println("BUILD_URL: " + BUILD_URL )
					println("STARTER_JOB_NAME: " + JOB_NAME )
					println("MY_BUILD_URL: " + CHANGE_URL )
					println("Component_Name: " + Component_Name )
					println("STARTER_BUILD_NUMBER: " + STARTER_BUILD_NUMBER )

					println("WHICHSECTION: " + WHICHSECTION )
					println("====== DEBUG 4 ======")

					def setupResult = build job: 'MoreTest/Jira_Job-Testing',
							parameters: [
							string(name: 'Component_Name', value: Component_Name.replace('"', '')),
							string(name: 'THERESULTS', value: THESTATE),
							string(name: 'THEJOBNAME', value: TheFAILINGJOB),
							string(name: 'THEJOBNUMBER', value: BUILD_NUMBER),
							string(name: 'BUILD_URL', value: BUILD_URL),
							string(name: 'STARTER_BUILD_NUMBER', value: STARTER_BUILD_NUMBER),
							string(name: 'STARTER_JOB_NAME', value: JOB_NAME),
							string(name: 'WHICHSECTION', value: WHICHSECTION),
							string(name: 'MY_BUILD_URL', value: CHANGE_URL)
							], propagate: true

					println("====== DEBUG 5 ======")

					try
					{
						def systest_build_number = setupResult.getNumber()
						env['setup_build_number'] = systest_build_number
						println("systest_build_number: " + systest_build_number)
						println("The Number: " + env['setup_build_number'])
					}
					catch(err)
					{
						JOBISGOOD = false
						currentBuild.result = 'SUCCESS'
						println("Error " + err)
					}
					println("====== DEBUG 6 ======")
					try
					{
						copyArtifacts fingerprintArtifacts: true, flatten: true, projectName: 'MoreTest/Jira_Job-Testing', selector: specific(env['setup_build_number'])

						TICKETNUMBER = sh returnStdout: true, script: 'cat ./CurrentState.txt'

						PostBuild( "Jira Ticket Number: " + TICKETNUMBER, "red")
						archiveArtifacts allowEmptyArchive: true, artifacts: 'CurrentState.txt'

						println("TICKETNUMBER: " + TICKETNUMBER)
						println("====== DEBUG 7 ======")

							if ( TICKETNUMBER != "" )
							{
								def JIRATICket = "<a href='https://example.jira.com/browse/" + TICKETNUMBER + "'>" + TICKETNUMBER + "</a>"
								addHtmlBadge html: JIRATICket, id: 'jira'
								manager.addShortText("BUILD FAILED", "red", "white", "0px", "white")
								manager.createSummary("red.gif").appendText("<h3><a href='https://example.jira.com/browse/" + TICKETNUMBER + "'>" + TICKETNUMBER + "</a></h3>", false, false, false, "red")
							}
							else
							{
								manager.addShortText("BUILD PASSED", "green", "white", "0px", "white")
								manager.createSummary("green.gif").appendText("<h3>BUILD PASSED</h3>", false, false, false, "green")
							}
					}
					catch(err)
					{
						JOBISGOOD = false
						currentBuild.result = 'FAILURE'
						error("DoJIRA Error " + err)
					}
					println("====== DEBUG 8 ======")
				}
				catch(err)
				{
					JOBISGOOD = false
					currentBuild.result = 'FAILURE'
					error("DoJIRA OVERALL Error " + err)
				}
			}
			if ( THESTATE == "FAILED")
			{
				currentBuild.result = 'FAILURE'
				error("DoJIRA Failing job is " + TheFAILINGJOB + " Exit now")
			}
		}
		else
		{
			println("SPECIAL is TURNED ON no JIRA TICKET")
		}
	println("================== END JIRA =======================")
}
def call(String THESTATE, String Component_Name, String TheFAILINGJOB, String WHICHSECTION, String STARTER_BUILD_NUMBER)
{
	println("THESTATE: = " + THESTATE)
	println("TheFAILINGJOB: = " + TheFAILINGJOB)
	println("WHICHSECTION: = " + WHICHSECTION)
	println("Component_Name: = " + Component_Name)
	println("STARTER_BUILD_NUMBER: = " + STARTER_BUILD_NUMBER)

	DoJIRA(THESTATE, Component_Name, TheFAILINGJOB, WHICHSECTION, STARTER_BUILD_NUMBER)
		if ( THESTATE == "FAILED" )
			SendMailNow()
}
return this


