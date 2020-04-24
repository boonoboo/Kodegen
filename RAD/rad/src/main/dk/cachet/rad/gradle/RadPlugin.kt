package dk.cachet.rad.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RadPlugin : Plugin<Project> {
	val RAD_TASK_NAME = "rad"

	override fun apply(project: Project) {
		project.tasks.register(RAD_TASK_NAME) {

		}
	}
}