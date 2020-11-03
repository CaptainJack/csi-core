plugins {
	kotlin("multiplatform") version "1.4.10" apply false
	id("nebula.release") version "15.3.0"
	id("ru.capjack.depver") version "1.2.0"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.5.0")
		"tool-utils"("1.0.0")
		"tool-io"("0.7.0")
		"tool-logging"("1.2.0")
	}
	"ch.qos.logback:logback-classic"("1.2.3")
}

subprojects {
	group = "ru.capjack.csi"
	
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
	}
	
	afterEvaluate {
		if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
			configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
				targets.forEach {
					if (it is org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget) {
						it.compilations.all { kotlinOptions.jvmTarget = "1.8" }
					}
				}
			}
		}
	}
}
