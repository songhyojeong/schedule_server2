package com.schedule.server;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = "com.schedule")
@EntityScan("com.schedule")
@EnableJpaRepositories("com.schedule")
public class ScheduleServerApplication {

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) {
		SpringApplication.run(ScheduleServerApplication.class, args);
	}

	@PostConstruct
	public void checkDatabaseConnection() {
	    try (var conn = dataSource.getConnection()) {
	        if (conn.isValid(0)) {
	            System.out.println("✅ DB 연결 성공: " + conn.getMetaData().getURL());
	        } else {
	            System.err.println("❌ DB 연결 실패: 유효하지 않은 연결");
	        }
	    } catch (Exception e) {
	        System.err.println("❌ DB 연결 실패: " + e.getMessage());
	    }
	}




}
