plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
	jacoco
}

kotlin {
	jvm {
		compilations.all { kotlinOptions.jvmTarget = "1.8" }
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.tool:tool-lang:0.5.0")
			implementation("ru.capjack.tool:tool-logging:0.14.0")
			
			api(project(":tool-csi-common"))
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
			implementation("ch.qos.logback:logback-classic:1.2.3")
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