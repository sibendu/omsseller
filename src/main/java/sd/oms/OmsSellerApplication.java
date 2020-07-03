package sd.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class OmsSellerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmsSellerApplication.class, args);
	}

}
