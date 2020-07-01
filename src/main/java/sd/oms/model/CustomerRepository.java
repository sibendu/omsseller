package sd.oms.model;

import org.springframework.data.repository.CrudRepository;
import java.util.*;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
	List<Customer> findByPhone(String phone);
	List<Customer> findBySeller(Long seller);
}
