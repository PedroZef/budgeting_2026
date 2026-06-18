package br.com.budgeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BudgetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetingApplication.class, args);
		System.out.println("🤖 Jarvis iniciado!");
		System.out.println("👉 Acesse o Frontend em: http://localhost:8085/");
		System.out.println("👉 Acesse o Swagger em:  http://localhost:8085/swagger-ui.html");
	}

}
