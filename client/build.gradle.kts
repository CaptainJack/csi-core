plugins {
	kotlin("multiplatform")
	id("ru.capjack.depver")
	jacoco
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation("ru.capjack.tool:tool-lang")
			implementation("ru.capjack.tool:tool-logging")
			api(project(":csi-core-common"))
		}
		get("commonTest").dependencies {
			implementation(kotlin("test"))
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("reflect"))
		}
		get("jvmTest").dependencies {
			implementation("ch.qos.logback:logback-classic")
		}
	}
}

jacoco {
	toolVersion = "0.8.7"
}

tasks.register<JacocoReport>("jvmTestCodeCoverageReport") {
	dependsOn(tasks["jvmTest"])
	executionData(tasks["jvmTest"])
	
	classDirectories.setFrom(
		"build/classes/kotlin/jvm/main"
	)
	sourceDirectories.setFrom(
		"src/commonMain/kotlin",
		"src/jvmMain/kotlin"
	)
}