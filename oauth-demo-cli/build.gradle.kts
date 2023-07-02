import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
	kotlin("jvm") version "1.8.22"
	application
}

repositories {
	mavenCentral()
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
    kotlin.srcDirs("src/main/kotlin")
	resources.srcDirs("src/main/resources")
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.+")
	implementation("org.slf4j:slf4j-api:2.0.7")
}

application {
	mainClass.set("th.in.jamievl.oauthclidemo.MainKt")
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
	}
}

tasks.withType<Copy>().configureEach {
	duplicatesStrategy = DuplicatesStrategy.WARN
}
