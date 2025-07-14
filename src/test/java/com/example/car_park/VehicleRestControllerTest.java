package com.example.car_park;

import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.model.Role;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.enums.ERole;
import com.example.car_park.service.JwtService;
import com.example.car_park.service.VehicleService;
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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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
public class VehicleRestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private VehicleService vehicleService;

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
    public void testGetVehicles() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.findAllForRest(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetVehiclesWithId() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.findByIdForRest(any(), any())).thenReturn(new VehicleResponseDto());

        mockMvc.perform(get("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetVehiclesByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(get("/api/vehicles")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetVehiclesByNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetVehiclesWithNotAvailableId() throws Exception {
        final Long notAvailableId = 999L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.findByIdForRest(user, notAvailableId))
                .thenThrow(new ResponseStatusException(FORBIDDEN,
                        String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", notAvailableId)));

        MvcResult result = mockMvc.perform(get("/api/vehicles/" + notAvailableId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", notAvailableId), errorMessage);
    }

    @Test
    public void testGetVehiclesWithNotExistingId() throws Exception {
        final Long notExistingId = 999L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.findByIdForRest(user, notExistingId))
                .thenThrow(new ResponseStatusException(NOT_FOUND,
                        String.format("Транспортное средство с id=%d отсутствует", notExistingId)));

        MvcResult result = mockMvc.perform(get("/api/vehicles/" + notExistingId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортное средство с id=%d отсутствует", notExistingId), errorMessage);
    }

    @Test
    void testCreateVehicle() throws Exception {
        Vehicle createdVehicle = new Vehicle();
        createdVehicle.setId(1L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.create(any(), any())).thenReturn(createdVehicle);
        mockMvc.perform(post("/api/vehicles/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                {
                                  "regNum": "H527EE",
                                  "price": 750000,
                                  "mileage": 170000,
                                  "productionYear": 2018,
                                  "color": "Чёрный",
                                  "enterpriseId": 2,
                                  "brandId": 2,
                                  "driverIds": [],
                                  "activeDriverId": null
                                }
                            """
                        ))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        "/api/vehicles/" + createdVehicle.getId()));
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

        mockMvc.perform(post("/api/vehicles/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateVehicleByNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/vehicles/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateVehicleWithBadRequest() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        mockMvc.perform(post("/api/vehicles/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": null,
                                      "price": -100,
                                      "productionYear": 2030,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": null,
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isBadRequest());
        // TODO: добавить проверку списка ошибок валидации
    }

    @Test
    void testCreateVehicleWithNotAvailableEnterprise() throws Exception {
        Long notAvailableEnterpriseId = 3L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.create(any(), any())).thenThrow(new ResponseStatusException(FORBIDDEN,
                String.format("Вы не управляете предприятием с id=%d", notAvailableEnterpriseId)));

        MvcResult result = mockMvc.perform(post("/api/vehicles/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 3,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Вы не управляете предприятием с id=%d", notAvailableEnterpriseId), errorMessage);
    }

    @Test
    void testCreateVehicleWithNotExistingBrand() throws Exception {
        Long notExistingBrandId = 99L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.create(any(), any())).thenThrow(new ResponseStatusException(NOT_FOUND,
                String.format("Бренд с id=%d отсутствует", notExistingBrandId)));

        MvcResult result = mockMvc.perform(post("/api/vehicles/new")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 3,
                                      "brandId": 99,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Бренд с id=%d отсутствует", notExistingBrandId), errorMessage);
    }

    @Test
    void testEditVehicle() throws Exception {
        VehicleResponseDto updatedVehicle = new VehicleResponseDto();

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.update(any(), any(), any())).thenReturn(updatedVehicle);
        mockMvc.perform(put("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isOk());
    }

    @Test
    void testEditVehicleByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(put("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEditVehicleByNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void testEditVehicleWithBadRequest() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        mockMvc.perform(put("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": null,
                                      "price": -100,
                                      "productionYear": 2030,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": null,
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isBadRequest());
        // TODO: добавить проверку списка ошибок валидации
    }

    @Test
    void testEditNotExistingVehicle() throws Exception {
        Long notExistingVehicleId = 99L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.update(any(), any(), any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Транспортное средство с id=%d отсутствует", notExistingVehicleId)));
        MvcResult result = mockMvc.perform(put("/api/vehicles/" + notExistingVehicleId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортное средство с id=%d отсутствует", notExistingVehicleId), errorMessage);
    }

    @Test
    void testEditNotAvailableVehicle() throws Exception {
        Long notAvailableVehicleId = 5L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.update(any(), any(), any())).thenThrow(new ResponseStatusException(FORBIDDEN,
                String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", notAvailableVehicleId)));
        MvcResult result = mockMvc.perform(put("/api/vehicles/" + notAvailableVehicleId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", notAvailableVehicleId), errorMessage);
    }

    @Test
    void testEditVehicleWithNotAvailableDrivers() throws Exception {
        Set<Long> notAvailableDriverIds = Set.of(3L, 4L);

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(vehicleService.update(any(), any(), any())).thenThrow(
                new ResponseStatusException(FORBIDDEN,
                        "Водители с id " + notAvailableDriverIds + " не относятся к Вашим предприятиям"));
        MvcResult result = mockMvc.perform(put("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                    {
                                      "regNum": "H527EE",
                                      "price": 750000,
                                      "mileage": 170000,
                                      "productionYear": 2018,
                                      "color": "Чёрный",
                                      "enterpriseId": 2,
                                      "brandId": 2,
                                      "driverIds": [2, 3, 4],
                                      "activeDriverId": null
                                    }
                                """
                        ))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals("Водители с id " + notAvailableDriverIds + " не относятся к Вашим предприятиям", errorMessage);
    }

    @Test
    public void testDeleteVehicle() throws Exception {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);

        mockMvc.perform(delete("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteVehicleByNotManager() throws Exception {
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        User notManager = new User()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .setRoles(Set.of(userRole));

        when(userDetailsService.loadUserByUsername(notManager.getUsername())).thenReturn(notManager);

        mockMvc.perform(delete("/api/vehicles/1")
                        .cookie(new Cookie("JWT", jwtService.generateToken(notManager))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteVehicleByNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteNotExistingVehicle() throws Exception {
        Long notExistingVehicleId = 99L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Транспортное средство с id=%d отсутствует", notExistingVehicleId)))
                .when(vehicleService)
                .delete(any(), any());
        MvcResult result = mockMvc.perform(delete("/api/vehicles/" + notExistingVehicleId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isNotFound())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортное средство с id=%d отсутствует", notExistingVehicleId), errorMessage);
    }

    @Test
    public void testDeleteNotAvailableVehicle() throws Exception {
        Long notAvailableVehicleId = 3L;

        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(new ResponseStatusException(FORBIDDEN,
                String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", notAvailableVehicleId)))
                .when(vehicleService)
                .delete(any(), any());
        MvcResult result = mockMvc.perform(delete("/api/vehicles/" + notAvailableVehicleId)
                        .cookie(new Cookie("JWT", jwtService.generateToken(user))))
                .andExpect(status().isForbidden())
                .andReturn();

        String errorMessage = result.getResponse().getErrorMessage();
        assertEquals(String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", notAvailableVehicleId), errorMessage);
    }
}
