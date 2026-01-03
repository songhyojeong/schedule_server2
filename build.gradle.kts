import org.gradle.api.tasks.compile.JavaCompile 

plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.LikeARainbow"
version = "1.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// core
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.springframework.boot:spring-boot-starter-validation")

// mail
implementation("org.springframework.boot:spring-boot-starter-mail")

// JWT (직접 Nimbus API를 쓰는 경우에만 유지)
implementation("com.nimbusds:nimbus-jose-jwt:9.31")

// OAuth2 소셜로그인 (카카오/구글 등 쓰면 유지)
implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

// mysql
implementation ("com.mysql:mysql-connector-j:9.0.0")

// (선택) 구글 API 실제 사용 시에만 유지
// implementation("com.google.api-client:google-api-client:1.33.0")
// implementation("com.google.oauth-client:google-oauth-client:1.34.1")
// implementation("com.google.http-client:google-http-client-gson:1.41.0")

// Lombok
compileOnly("org.projectlombok:lombok")
annotationProcessor("org.projectlombok:lombok")

// dev & test
developmentOnly("org.springframework.boot:spring-boot-devtools")
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.security:spring-security-test")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}