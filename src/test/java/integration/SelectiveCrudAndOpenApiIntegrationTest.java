package integration;

import com.rokaidev.fasterapi.annotation.FasterCRUD;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SelectiveCrudAndOpenApiIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "fasterapi.base-package=integration",
        "spring.datasource.url=jdbc:h2:mem:selective-test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SelectiveCrudAndOpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Entity
    @FasterCRUD(
            path = "/selective-products",
            enableGet = true,
            enablePost = true,
            enablePut = true,
            enablePatch = true,
            enableDelete = false)
    static class SelectiveProduct {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity
    @FasterCRUD(path = "/no-get", enableGet = false)
    static class NoGet {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    }

    @Entity
    @FasterCRUD(path = "/no-post", enablePost = false)
    static class NoPost {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    }

    @Entity
    @FasterCRUD(path = "/no-put", enablePut = false)
    static class NoPut {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    }

    @Entity
    @FasterCRUD(path = "/no-patch", enablePatch = false)
    static class NoPatch {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @Test
    void disabledDeleteReturnsNotFoundAndOtherOperationsRemainAvailable() throws Exception {
        mockMvc.perform(delete("/selective-products/1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/selective-products")
                        .contentType("application/json")
                        .content("{\"name\":\"Produto\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void eachDisabledOperationIsUnmapped() throws Exception {
        mockMvc.perform(get("/no-get")).andExpect(status().isNotFound());
        mockMvc.perform(get("/no-get/1")).andExpect(status().isNotFound());
        mockMvc.perform(post("/no-post").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/no-put/1").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/no-patch/1").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void openApiContainsOnlyRegisteredOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/selective-products'].get").exists())
                .andExpect(jsonPath("$.paths['/selective-products'].post").exists())
                .andExpect(jsonPath("$.paths['/selective-products/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/selective-products/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/selective-products/{id}'].patch").exists())
                .andExpect(jsonPath("$.paths['/selective-products/{id}'].delete").doesNotExist())
                .andExpect(jsonPath("$.paths['/selective-products'].get.tags", hasItem("SelectiveProduct")));
    }
}
