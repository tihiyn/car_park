package com.example.car_park;

import com.example.car_park.dao.model.User;
import com.example.car_park.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    public void testAuthenticateWithWrongPassword() throws Exception {
        String username = "username";
        String wrongPassword = "password";

        when(authenticationService.authenticate(any())).thenThrow(BadCredentialsException.class);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", wrongPassword))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuthenticate() throws Exception {
        User user = new User()
                .setUsername("username")
                .setPassword("encryptedPassword")
                .setRoles(Set.of());

        when(authenticationService.authenticate(any())).thenReturn(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "username")
                        .param("password", "password1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/enterprises"));
    }


    // TODO: создать класс AuthenticationServiceTest и перенести туда логику закомментированных тестов
//    @Test
//    public void testAuthenticateWithWrongPassword() throws Exception {
//        String username = "username";
//        String wrongPassword = "password";
//
////        when(authenticationService.authenticate(any())).thenThrow(BadCredentialsException.class);
//
//        mockMvc.perform(post("/auth/login")
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .param("username", username)
//                .param("password", wrongPassword))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void testAuthenticate() throws Exception {
//        User user = new User()
//                .setUsername("username")
//                .setPassword("$2a$10$R3/FIqXARnpJXcRHoe9Qlu51qc7QaQuCZKWyi.PqDkQhaAQqa0NZa")
//                .setRoles(Set.of());
//
//        when(userDetailsService.loadUserByUsername("username")).thenReturn(user);
//        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
////        when(authenticationService.authenticate(any())).thenThrow(BadCredentialsException.class);
//
//        mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                        .param("username", "username")
//                        .param("password", "password1"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/api/enterprises"));
//    }

}
