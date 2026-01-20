package sky.spring.transaction_lock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@Transactional
public class TransactionLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionLockApplication.class, args);
	}

}
