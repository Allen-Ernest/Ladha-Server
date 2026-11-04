package tz.co.ladha;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LadhaApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testRegisterAdmin_MissingPassword() throws Exception {
		// The exact body the user tried in Postman
		String body = "{\"email\":\"allenernest360@gmail.com\",\"firstName\":\"Allen\",\"lastName\":\"Ernest\",\"phone\":\"+255612211543\",\"gender\":\"male\",\"role\":\"admin\",\"adminType\":\"main-admin\"}";
		mockMvc.perform(post("/api/auth/register-admin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void testRegisterAdmin_Success() throws Exception {
		String uniqueEmail = "user_" + System.currentTimeMillis() + "@example.com";
		String uniquePhone = "+255" + (System.currentTimeMillis() % 1000000000L);
		String body = String.format("{\"email\":\"%s\",\"firstName\":\"Allen\",\"lastName\":\"Ernest\",\"phone\":\"%s\",\"gender\":\"male\",\"role\":\"admin\",\"adminType\":\"main-admin\",\"password\":\"password123\",\"confirmPassword\":\"password123\"}", uniqueEmail, uniquePhone);
		mockMvc.perform(post("/api/auth/register-admin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andDo(print())
				.andExpect(status().isCreated());
	}

}
