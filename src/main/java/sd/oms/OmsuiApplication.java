package sd.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class OmsuiApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmsuiApplication.class, args);
	}

}
