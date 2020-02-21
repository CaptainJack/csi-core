import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
	kotlin("multiplatform") version "1.3.61" apply false
	id("nebula.release") version "14.0.4"
	id("ru.capjack.depver") version "1.0.0"
	id("ru.capjack.logging") version "1.0.1"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.1.0")
		"tool-utils"("0.6.0-dev.1.uncommitted+8ac309c")
		"tool-io"("0.6.0-dev.0.uncommitted+48f55be")
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
	
	tasks.withType<KotlinJvmCompile> {
		kotlinOptions.jvmTarget = "11"
	}
	
	tasks.withType<KotlinJsCompile> {
		kotlinOptions.sourceMap = false
	}
}
