package fit.hutech.spring;

import fit.hutech.spring.entities.Role;
import fit.hutech.spring.repositories.IRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(IRoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("USER") == null) {
				roleRepository.save(new Role("USER"));
			}
			if (roleRepository.findByName("ADMIN") == null) {
				roleRepository.save(new Role("ADMIN"));
			}
			System.out.println("Database connection successful. Roles initialized.");
		};
	}
}
