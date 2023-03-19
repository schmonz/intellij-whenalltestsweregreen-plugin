package com.schmonz.intellij.whenalltestsweregreen.listeners

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.CompilerProjectExtension
import java.io.File
import java.nio.file.attribute.FileTime
import java.time.Instant
import kotlin.io.path.setLastModifiedTime

internal class TestRunEventListener : TestStatusListener() {

    override fun testSuiteFinished(root: AbstractTestProxy?) =
        notifyUser("When All Tests Were Green: no project associated with those tests!")

    override fun testSuiteFinished(root: AbstractTestProxy?, project: Project) {
        if (everyTestThatRanWasGreen(root)) { // && thatWasReallyAllTheTests()
            updateLatestGreenTestsTimestamp(project)
        }
    }

    private fun notifyUser(message: String) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("com.schmonz.intellij.whenalltestsweregreen")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(ProjectManager.getInstance().defaultProject)
    }

    private fun everyTestThatRanWasGreen(root: AbstractTestProxy?) =
        root!!.allTests.all { it.hasPassedTests() }

    private fun updateLatestGreenTestsTimestamp(project: Project) {
        val timestampFile = File(
            CompilerProjectExtension.getInstance(project)!!.compilerOutput!!.path,
            "when-all-tests-were-green"
        )
        if (!timestampFile.createNewFile()) {
            timestampFile.toPath().setLastModifiedTime(FileTime.from(Instant.now()))
        }
        notifyUser("updated timestamp on ${timestampFile.path}")
    }
}
