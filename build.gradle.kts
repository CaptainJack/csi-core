plugins {
	kotlin("multiplatform") version "1.6.21" apply false
	id("ru.capjack.publisher") version "1.0.0"
	id("ru.capjack.depver") version "1.2.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.12.0")
		"tool-logging"("1.6.0")
		"tool-utils"("1.8.0")
		"tool-io"("1.1.0")
	}
	"ch.qos.logback:logback-classic"("1.2.11")
}

subprojects {
	group = "ru.capjack.csi"
	
	repositories {
		mavenCentral()
		mavenCapjack()
	}
	
	afterEvaluate {
		if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
			configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
				targets.forEach {
					if (it is org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget) {
						it.compilations.all { kotlinOptions.jvmTarget = "11" }
					}
				}
			}
		}
	}
}
