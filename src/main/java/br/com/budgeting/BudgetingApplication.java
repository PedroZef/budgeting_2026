package br.com.budgeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BudgetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetingApplication.class, args);
		System.out.println("🤖 Jarvis iniciado!acesse: http://localhost:8080/swagger-ui/index.html");
	}

}
