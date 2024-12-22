package com.studenthive.studentHiveWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudentHiveWebApplication {

	public static void main(String[] args) {
		MongoDBManager mongoDBManager = new MongoDBManager();
		var schedulesCollection = mongoDBManager.getCollection("schedules");
		SpringApplication.run(StudentHiveWebApplication.class, args);
	}

}
