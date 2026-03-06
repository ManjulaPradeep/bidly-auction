package com.bidly.auction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {
			"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
			"spring.datasource.driverClassName=org.h2.Driver",
			"spring.datasource.username=sa",
			"spring.datasource.password=",
			"spring.jpa.hibernate.ddl-auto=create-drop",
			"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
			"spring.flyway.enabled=false",
			"app.jwt.secret=TestSecretKeyAtLeast32CharactersLong123"
		})
class AuctionApplicationTests {

	@Test
	void contextLoads() {
	}

}
