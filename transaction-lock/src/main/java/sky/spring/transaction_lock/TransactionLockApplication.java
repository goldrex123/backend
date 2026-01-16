package sky.spring.transaction_lock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionLockApplication {

	public static void main(String[] args) {
		System.out.println("Hello World");
		SpringApplication.run(TransactionLockApplication.class, args);
	}

}
