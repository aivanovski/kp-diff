import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "4.0.4"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    jacoco
}

val appVersion = "0.6.1"

group = "com.github.ai.kpdiff"
version = appVersion

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.jacocoTestReport {
    reports {
        val coverageDir = File("$buildDir/reports/coverage")
        csv.required.set(true)
        csv.outputLocation.set(File(coverageDir, "coverage.csv"))
        html.required.set(true)
        html.outputLocation.set(coverageDir)
    }

    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it).matching {
                exclude("com/github/ai/kpdiff/di/**")
            }
        }
    )

    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}

tasks.classes {
    dependsOn("createPropertyFileWithVersion")
}

tasks.register("createPropertyFileWithVersion") {
    doLast {
        val propertyName = "version"
        val propsFile = File("$projectDir/src/main/resources/version.properties")
        val props = Properties()

        if (propsFile.exists()) {
            props.load(FileInputStream(propsFile))
        }

        if (props[propertyName] != appVersion) {
            project.logger.lifecycle("Updating file: version.properties")
            props[propertyName] = appVersion
            val parentFile = propsFile.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            props.store(BufferedWriter(FileWriter(propsFile)), "File is generated by Gradle")
        }
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("kp-diff")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.github.ai.kpdiff.MainKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

detekt {
    config.setFrom("detekt.yml")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.2")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.2")
    testImplementation("io.mockk:mockk:1.12.3")

    implementation("io.insert-koin:koin-core:3.1.5")
    implementation("com.github.aivanovski:keepass-tree-diff:0.3.0")
    implementation("com.github.aivanovski:keepass-tree-builder:0.2.0")
    implementation("com.github.anvell:kotpass:0.7.0")
}