package com.example.car_park;

import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Role;
import com.example.car_park.dao.model.User;
import com.example.car_park.enums.ERole;
import com.example.car_park.service.EnterpriseService;
import com.example.car_park.service.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EnterpriseRestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private EnterpriseService enterpriseService;

    private static User user;

    @BeforeAll
    public static void createManager() {
        Role managerRole = new Role();
        managerRole.setName(ERole.ROLE_MANAGER);

        user = new User()
                .setUsername("username")
                .setPassword("password")
                .setRoles(Set.of(managerRole));

    }

    @Test
    public void testGetEnterprises() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.findAllForRest(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/enterprises")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEnterprisesWithId() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.findByIdForRest(any(), any())).thenReturn(new EnterpriseResponseDto());

        mockMvc.perform(get("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEnterprisesByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(get("/api/enterprises")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetEnterprisesByNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/enterprises"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetEnterprisesWithNotAvailableId() throws Exception {
        final Long notAvailableId = 999L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.findByIdForRest(user, notAvailableId))
                .thenThrow(new ResponseStatusException(FORBIDDEN,
                        String.format("Вы не управляете предприятием с id=%d", notAvailableId)));

        MvcResult result = mockMvc.perform(get("/api/enterprises/" + notAvailableId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Вы не управляете предприятием с id=%d", notAvailableId), errorMessage);
    }

    @Test
    public void testGetEnterprisesWithNotExistingId() throws Exception {
        final Long notExistingId = 4L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.findByIdForRest(user, notExistingId))
                .thenThrow(new ResponseStatusException(NOT_FOUND,
                        String.format("Предприятие с id=%d отсутствует", notExistingId)));

        MvcResult result = mockMvc.perform(get("/api/enterprises/" + notExistingId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Предприятие с id=%d отсутствует", notExistingId), errorMessage);
    }

    @Test
    void testCreateEnterprise() throws Exception {
        Enterprise createdEnterprise = new Enterprise();
        createdEnterprise.setId(1L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.create(any(), any())).thenReturn(createdEnterprise);
        mockMvc.perform(post("/api/enterprises/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        "/api/enterprises/" + createdEnterprise.getId()));
    }

    @Test
    void testCreateVehicleByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(post("/api/enterprises/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateVehicleByNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/enterprises/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateVehicleWithBadRequest() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        mockMvc.perform(post("/api/enterprises/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "",
                                      "city": null,
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": null
                                    }
                                """
                        ))
                .andExpect(status().isBadRequest());
        // TODO: добавить проверку списка ошибок валидации
    }

    @Test
    void testCreateVehicleWithNotAvailableVehicles() throws Exception {
        Set<Long> notAvailableVehicleIds = Set.of(3L, 7L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.create(any(), any())).thenThrow(new ResponseStatusException(FORBIDDEN,
                String.format("Транспортные средства с id %s не относятся к Вашим предприятиям", notAvailableVehicleIds)));

        MvcResult result = mockMvc.perform(post("/api/enterprises/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3, 7],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортные средства с id %s не относятся к Вашим предприятиям", notAvailableVehicleIds), errorMessage);
    }

    @Test
    void testCreateVehicleWithNotExistingDrivers() throws Exception {
        Set<Long> notExistingDriverIds = Set.of(99L, 100L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.create(any(), any())).thenThrow(new ResponseStatusException(NOT_FOUND,
                String.format("Водители с id %s отсутствуют", notExistingDriverIds)));

        MvcResult result = mockMvc.perform(post("/api/enterprises/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3, 7],
                                      "driverIds": [2, 99, 100]
                                    }
                                """
                        ))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Водители с id %s отсутствуют", notExistingDriverIds), errorMessage);
    }

    @Test
    void testEditEnterprise() throws Exception {
        EnterpriseResponseDto updatedEnterprise = new EnterpriseResponseDto();

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.update(any(), any(), any())).thenReturn(updatedEnterprise);
        mockMvc.perform(put("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isOk());
    }

    @Test
    void testEditEnterpriseByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(put("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEditEnterpriseByNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/enterprises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEditEnterpriseWithBadRequest() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        mockMvc.perform(put("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "",
                                      "city": null,
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": null
                                    }
                                """
                        ))
                .andExpect(status().isBadRequest());
        // TODO: добавить проверку списка ошибок валидации
    }

    @Test
    void testEditNotExistingEnterprise() throws Exception {
        Long notExistingEnterpriseId = 99L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.update(any(), any(), any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Предприятие с id=%d отсутствует", notExistingEnterpriseId)));
        MvcResult result = mockMvc.perform(put("/api/enterprises/" + notExistingEnterpriseId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Предприятие с id=%d отсутствует", notExistingEnterpriseId), errorMessage);
    }

    @Test
    void testEditNotAvailableEnterprise() throws Exception {
        Long notAvailableEnterpriseId = 5L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.update(any(), any(), any())).thenThrow(new ResponseStatusException(FORBIDDEN,
                String.format("Вы не управляете предприятием с id=%d", notAvailableEnterpriseId)));
        MvcResult result = mockMvc.perform(put("/api/enterprises/" + notAvailableEnterpriseId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2, 3],
                                      "driverIds": [2, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Вы не управляете предприятием с id=%d", notAvailableEnterpriseId), errorMessage);
    }

    @Test
    void testEditEnterpriseWithNotAvailableVehicles() throws Exception {
        Set<Long> notAvailableDriverIds = Set.of(3L, 4L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(enterpriseService.update(any(), any(), any())).thenThrow(
                new ResponseStatusException(FORBIDDEN,
                        "Водители с id " + notAvailableDriverIds + " не относятся к Вашим предприятиям"));
        MvcResult result = mockMvc.perform(put("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "name": "Грузовичок",
                                      "city": "Москва",
                                      "registrationNumber": "0123456789",
                                      "vehicleIds": [1, 2],
                                      "driverIds": [2, 3, 4]
                                    }
                                """
                        ))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals("Водители с id " + notAvailableDriverIds + " не относятся к Вашим предприятиям", errorMessage);
    }

    @Test
    public void testDeleteEnterprise() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);

        mockMvc.perform(delete("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEnterpriseByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(delete("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteEnterpriseByNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/enterprises/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteNotExistingEnterprise() throws Exception {
        Long notExistingEnterpriseId = 99L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Предприятие с id=%d отсутствует", notExistingEnterpriseId)))
                .when(enterpriseService)
                .delete(any(), any());
        MvcResult result = mockMvc.perform(delete("/api/enterprises/" + notExistingEnterpriseId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Предприятие с id=%d отсутствует", notExistingEnterpriseId), errorMessage);
    }

    @Test
    public void testDeleteNotAvailableEnterprise() throws Exception {
        Long notAvailableEnterpriseId = 3L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(new ResponseStatusException(FORBIDDEN,
                String.format("Вы не управляете предприятием с id=%d", notAvailableEnterpriseId)))
                .when(enterpriseService)
                .delete(any(), any());
        MvcResult result = mockMvc.perform(delete("/api/enterprises/" + notAvailableEnterpriseId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Вы не управляете предприятием с id=%d", notAvailableEnterpriseId), errorMessage);
    }

    @Test
    public void testDeleteEnterpriseWithExistingVehicles() throws Exception {
        List<Long> existingVehicles = List.of(5L, 3L, 10L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(new ResponseStatusException(CONFLICT,
                String.format("Нельзя удалить предприятие: в нём есть транспортные средства %s и/или водители %s!",
                        existingVehicles, new ArrayList<>())))
                .when(enterpriseService)
                .delete(any(), any());
        MvcResult result = mockMvc.perform(delete("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isConflict())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Нельзя удалить предприятие: в нём есть транспортные средства %s и/или водители %s!", existingVehicles, new ArrayList<>()), errorMessage);
    }

    @Test
    public void testDeleteEnterpriseWithExistingDrivers() throws Exception {
        List<Long> existingDrivers = List.of(1L, 4L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(new ResponseStatusException(CONFLICT,
                String.format("Нельзя удалить предприятие: в нём есть транспортные средства %s и/или водители %s!",
                        new ArrayList<>(), existingDrivers)))
                .when(enterpriseService)
                .delete(any(), any());
        MvcResult result = mockMvc.perform(delete("/api/enterprises/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isConflict())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Нельзя удалить предприятие: в нём есть транспортные средства %s и/или водители %s!", new ArrayList<>(), existingDrivers), errorMessage);
    }
}

