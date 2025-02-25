plugins {
    id("java")
    application
}

group = "com.jdriven"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
}

sourceSets {
    create("processor-dependencies") {
        java.srcDirs("src/annotations/java")
    }
    create("processor") {
        java.srcDirs("src/processor/java")
        compileClasspath += sourceSets["processor-dependencies"].output
    }
}

tasks.register<Jar>("processorJar") {
    archiveClassifier.set("processor")
    from(sourceSets["processor"].output)
    from(sourceSets["processor-dependencies"].output)
}

dependencies {
    implementation(sourceSets["processor-dependencies"].output)
    annotationProcessor(files(tasks.named("processorJar")))
}

application {
    mainClass.set("org.jdriven.Starter")
}
