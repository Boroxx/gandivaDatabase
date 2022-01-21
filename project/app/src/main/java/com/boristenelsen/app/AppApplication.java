package com.boristenelsen.app;

import com.boristenelsen.app.database.services.DumpReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import java.io.File;

@SpringBootApplication
public class AppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		File file = ResourceUtils.getFile("classpath:dump.sql");
		System.out.println(file.getAbsolutePath());
		DumpReader dumpReader = new DumpReader(file);
		dumpReader.initDatabase();
	}
}
