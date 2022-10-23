import java.nio.file.Path

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.SortKeys
import com.kazurayam.materialstore.inspector.Inspector
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

/**
 * Test Cases/main/DuckDuckGo/4_report
 *
 */

assert store != null
assert materialList != null

JobName jobName = materialList.getJobName()
JobTimestamp jobTimestamp = materialList.getJobTimestamp()

WebUI.comment("report started; materialList=${materialList.toString()}, jobName=${jobName}, store=${store}")

Inspector inspector = Inspector.newInstance(store)
SortKeys sortKeys = new SortKeys("step","URL.host", "URL.path")
inspector.setSortKeys(sortKeys)
Path report = inspector.report(materialList)

return report