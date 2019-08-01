plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
	id("ru.capjack.logging")
	jacoco
}

kotlin {
	jvm()
	js()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.tool:tool-lang")
			implementation("ru.capjack.tool:tool-utils")
			implementation("ru.capjack.tool:tool-logging")
			
			api(project(":csi-core-common"))
		}
		get("commonTest").dependencies {
			implementation(kotlin("test-common"))
			implementation(kotlin("test-annotations-common"))
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("reflect"))
			implementation(kotlin("stdlib-jdk8"))
		}
		get("jvmTest").dependencies {
			implementation(kotlin("test-junit"))
			implementation("ch.qos.logback:logback-classic")
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
		}
	}
}

tasks.register<JacocoReport>("jvmTestCodeCoverageReport") {
	dependsOn(tasks["jvmTest"])
	executionData(tasks["jvmTest"])
	classDirectories.setFrom("build/classes/kotlin/jvm/main")
	sourceDirectories.setFrom(
		"src/commonMain/kotlin",
		"src/jvmMain/kotlin"
	)
}

tasks.create<Copy>("jvmTestCopyResources") {
	//TODO https://youtrack.jetbrains.com/issue/KT-24463
	dependsOn("jvmTestProcessResources")
	from("build/processedResources/")
	into("build/classes/kotlin")
}