plugins {
    kotlin("jvm") version "1.3.72" apply false
    kotlin("kapt") version "1.3.72" apply false
    kotlin("plugin.serialization") version "1.3.72" apply false
}

subprojects {
    group = "dk.cachet"
    version = "1.0.2"

    repositories {
        // Include the local repository containing rad-1.0.1
        maven {
            url = uri(rootProject.projectDir.path + "/localRepository")
        }
        mavenCentral()
        jcenter()
    }
}

group = "dk.cachet"
version = "1.0.2"

// Class for tracking the execution time of each task
// Adapted from https://stackoverflow.com/questions/13031538/track-execution-time-per-task-in-gradle-script

// Log timings per task.
class TimingsListener : TaskExecutionListener, BuildListener {
    private var startTime = java.time.LocalDateTime.now()
    private val timings = mutableListOf<Pair<String, java.time.Duration>>()

    override fun beforeExecute(task: Task) {
        startTime = java.time.LocalDateTime.now()
    }

    override fun afterExecute(task: Task, taskState: TaskState) {
        val endTime = java.time.LocalDateTime.now()
        val runtime = java.time.Duration.between(startTime, endTime)
        timings += Pair(task.path, runtime)
        //task.project.logger.warn("${task.path} took ${runtime}")
    }

    override fun settingsEvaluated(p0: Settings) {
        return
    }

    override fun buildFinished(result: BuildResult) {
        println("Task timings:")
        timings.forEach { timing ->
            println("${timing.first} took ${timing.second}")
        }
        val totalTime = timings.fold(
                java.time.Duration.ZERO,
                { acc, cur -> acc.plus(cur.second) })
        println("Total build time: $totalTime")
    }



    override fun projectsLoaded(p0: Gradle) {
        return
    }

    override fun buildStarted(p0: Gradle) {
        return
    }

    override fun projectsEvaluated(p0: Gradle) {
        return
    }
}


gradle.addListener(TimingsListener())