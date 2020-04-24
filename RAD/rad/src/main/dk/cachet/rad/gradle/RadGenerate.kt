package dk.cachet.rad.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class RadGenerate : DefaultTask() {
	@Input
	lateinit var baseUrl: String

	@TaskAction
	fun processAnnotations() {

	}


}