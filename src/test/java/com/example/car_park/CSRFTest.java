package com.example.car_park;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
@SpringBootTest
@AutoConfigureMockMvc
class CSRFTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser(username = "АнисимовВС", password = "password1")
	void testSaveBrandIsOk() throws Exception {
		mockMvc.perform(post("/api/brands/save")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"
						+ "\"name\":\"Toyota Corolla\","
						+ "\"type\":\"Седан\","
						+ "\"transmission\":\"AUTOMATIC\","
						+ "\"engineVolume\":1.8,"
						+ "\"enginePower\":140,"
						+ "\"numOfSeats\":5"
						+ "}"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/brands"));
	}

	@Test
	@WithMockUser(username = "АнисимовВС", password = "password1")
	void testSaveBrandWithoutCSRF() throws Exception {
		mockMvc.perform(post("/api/brands/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"
								+ "\"name\":\"Toyota Corolla\","
								+ "\"type\":\"Седан\","
								+ "\"transmission\":\"AUTOMATIC\","
								+ "\"engineVolume\":1.8,"
								+ "\"enginePower\":140,"
								+ "\"numOfSeats\":5"
								+ "}"))
				.andExpect(status().isForbidden());
	}
}
