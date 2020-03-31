plugins {
	kotlin("multiplatform") version "1.3.71" apply false
	id("nebula.release") version "14.1.0"
	id("ru.capjack.depver") version "1.0.0"
	id("ru.capjack.logging") version "1.1.0"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.2.0")
		"tool-utils"("0.9.0")
		"tool-io"("0.6.1")
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
					else if (it is org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget) {
						it.compilations.all { kotlinOptions.sourceMap = false }
					}
				}
			}
		}
	}
}
