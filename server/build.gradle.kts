plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
	jacoco
}

kotlin {
	jvm()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation("ru.capjack.tool:tool-lang")
			implementation("ru.capjack.tool:tool-logging")
			api(project(":csi-core-common"))
		}
		get("commonTest").dependencies {
			implementation(kotlin("test-common"))
			implementation(kotlin("test-annotations-common"))
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		get("jvmTest").dependencies {
			implementation(kotlin("test-junit"))
			implementation("ch.qos.logback:logback-classic")
		}
	}
}

jacoco {
	toolVersion = "0.8.5"
}

tasks.register<JacocoReport>("jvmTestCodeCoverageReport") {
	dependsOn(tasks["jvmTest"])
	executionData(tasks["jvmTest"])
	
	val common = project(":csi-core-common").projectDir
	
	classDirectories.setFrom(
		"build/classes/kotlin/jvm/main",
		common.resolve("build/classes/kotlin/jvm/main")
	)
	sourceDirectories.setFrom(
		"src/commonMain/kotlin",
		"src/jvmMain/kotlin",
		common.resolve("src/commonMain/kotlin"),
		common.resolve("src/jvmMain/kotlin")
	)
}