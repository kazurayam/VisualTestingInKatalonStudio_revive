import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.ks.globalvariable.ExecutionProfilesLoader
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable

/**
 * Test Cases/main/MyAdmin/Main_Twins
 *
 */

Path projectDir = Paths.get(RunConfiguration.getProjectDir())
Path root = projectDir.resolve("store")
Store store = Stores.newInstance(root)
JobName jobName = new JobName("MyAdmin_Main_Twins")

// --------------------------------------------------------------------
/*
 * Materialize stage
 */
ExecutionProfilesLoader profilesLoader = new ExecutionProfilesLoader()

// visit the Production environment to materialize web resources
String profileP = "MyAdmin_ProductionEnv"
profilesLoader.loadProfile(profileP)
WebUI.comment("Execution Profile ${profileP} was loaded")
WebUI.comment("GlobalVariable.CSV=${GlobalVariable.CSV}")
File targetFileP = new File(GlobalVariable.CSV)
JobTimestamp timestampP = JobTimestamp.now()
WebUI.callTestCase(
	findTestCase("main/MyAdmin/materialize"),
	["profile":profileP, "targetFile": targetFileP, 
		"store": store, "jobName": jobName, "jobTimestamp": timestampP]
)

// visit the Development environment to materialize web resources
String profileD = "MyAdmin_DevelopmentEnv"
profilesLoader.loadProfile(profileD)
WebUI.comment("Execution Profile ${profileD} was loaded")
WebUI.comment("GlobalVariable.CSV=${GlobalVariable.CSV}")
File targetFileD = new File(GlobalVariable.CSV)
JobTimestamp timestampD = JobTimestamp.laterThan(timestampP)
WebUI.callTestCase(
	findTestCase("main/MyAdmin/materialize"),
	["profile":profileD, "targetFile": targetFileD, 
		"store": store, "jobName": jobName, "jobTimestamp": timestampD]
)
	
// --------------------------------------------------------------------
/*
 * Reduce stage
 */
// identify 2 MaterialList objects: left and right = production and development
// compare the right(development) against the left(project)
// find differences betwee the 2 versions --- Twins mode
MaterialList left = store.select(jobName, timestampP,
			QueryOnMetadata.builder([ "profile": profileP ]).build()
			)
MaterialList right = store.select(jobName, timestampD,
			QueryOnMetadata.builder([ "profile": profileD ]).build()
			)

WebUI.comment("left=${left.toString()}")
WebUI.comment("right=${right.toString()}")

MProductGroup reduced =
	WebUI.callTestCase(findTestCase("main/MyAdmin/reduceTwins"),
		["store": store,
			"leftMaterialList": left,
			"rightMaterialList": right
			])


//---------------------------------------------------------------------
/*
 * reporting stage
 */
// compile a human-readable report
int warnings =
	WebUI.callTestCase(findTestCase("main/MyAdmin/report"),
		["store": store, "mProductGroup": reduced, "criteria": 0.0d])



//---------------------------------------------------------------------
/*
 * Epilogue	
 */
if (warnings > 0) {
	KeywordUtil.markWarning("found ${warnings} differences.")
}

	