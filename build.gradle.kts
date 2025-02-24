plugins {
    id("java")
    application
}

group = "com.jdriven"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
}

sourceSets {
    create("processor-annotations") {
        java.srcDirs("src/annotations/java")
    }
    create("processor") {
        java.srcDirs("src/processor/java")
        compileClasspath += sourceSets["processor-annotations"].output
    }
}

tasks.register<Jar>("processorJar") {
    archiveClassifier.set("processor")
    from(sourceSets["processor"].output)
    from(sourceSets["processor-annotations"].output)
}

dependencies {
    implementation(sourceSets["processor-annotations"].output)
    annotationProcessor(files(tasks.named("processorJar")))
}

application {
    mainClass.set("org.jdriven.Starter")
}
