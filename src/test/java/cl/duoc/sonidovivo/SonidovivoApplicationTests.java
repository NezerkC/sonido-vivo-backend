package cl.duoc.sonidovivo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires live Oracle and MongoDB infrastructure; offline suite runs unit and ArchUnit tests.")
class SonidovivoApplicationTests {

	@Test
	void contextLoads() {
	}

}
