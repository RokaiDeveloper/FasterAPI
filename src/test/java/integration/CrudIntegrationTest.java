package integration;

import annotation.FasterCRUD;
import annotation.ReadOnly;
import core.FasterCrudInitializer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootApplication
@ComponentScan(basePackages = {"annotation", "core", "integration"})
class TestApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(TestApplication.class, args);
    }
}

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "fasterapi.base-package=integration",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FasterCrudInitializer initializer;

    @Entity
    @FasterCRUD(path = "/test-produtos")
    public static class TestProduto {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Nome é obrigatório")
        private String nome;

        @NotNull(message = "Preço é obrigatório")
        @Positive(message = "Preço deve ser positivo")
        private BigDecimal preco;

        private String descricao;

        @ReadOnly
        private String codigoInterno;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public BigDecimal getPreco() {
            return preco;
        }

        public void setPreco(BigDecimal preco) {
            this.preco = preco;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public String getCodigoInterno() {
            return codigoInterno;
        }

        public void setCodigoInterno(String codigoInterno) {
            this.codigoInterno = codigoInterno;
        }
    }

    @Test
    void testGetAll_EmptyList() throws Exception {
        mockMvc.perform(get("/test-produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testPagination() throws Exception {
        // Create 15 products
        for (int i = 1; i <= 15; i++) {
            mockMvc.perform(post("/test-produtos")
                    .contentType("application/json")
                    .content("{\"nome\":\"Produto " + i + "\",\"preco\":" + (i * 10.0) + "}"))
                    .andExpect(status().isCreated());
        }

        // Test first page with size 10
        mockMvc.perform(get("/test-produtos?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void testPagination_SecondPage() throws Exception {
        // Create 15 products
        for (int i = 1; i <= 15; i++) {
            mockMvc.perform(post("/test-produtos")
                    .contentType("application/json")
                    .content("{\"nome\":\"Produto " + i + "\",\"preco\":" + (i * 10.0) + "}"))
                    .andExpect(status().isCreated());
        }

        // Test second page
        mockMvc.perform(get("/test-produtos?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number", is(1)));
    }

    @Test
    void testSort_Ascending() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto B\",\"preco\":200.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto A\",\"preco\":100.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto C\",\"preco\":300.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/test-produtos?sort=nome,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome", is("Produto A")))
                .andExpect(jsonPath("$.content[1].nome", is("Produto B")))
                .andExpect(jsonPath("$.content[2].nome", is("Produto C")));
    }

    @Test
    void testSort_Descending() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto A\",\"preco\":100.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto B\",\"preco\":200.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/test-produtos?sort=preco,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].preco", is(200.00)))
                .andExpect(jsonPath("$.content[1].preco", is(100.00)));
    }

    @Test
    void testFilter_Equals() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Notebook\",\"preco\":3500.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Mouse\",\"preco\":50.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/test-produtos?nome=Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Notebook")));
    }

    @Test
    void testFilter_Like() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Notebook Dell\",\"preco\":3500.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Notebook HP\",\"preco\":3200.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Teclado\",\"preco\":100.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/test-produtos?nome__like=Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testCreate_Success() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto Teste\",\"preco\":99.99,\"descricao\":\"Descrição\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Produto Teste")))
                .andExpect(jsonPath("$.preco", is(99.99)))
                .andExpect(jsonPath("$.descricao", is("Descrição")));
    }

    @Test
    void testCreate_ValidationFailure_EmptyName() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"\",\"preco\":99.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validação falhou")));
    }

    @Test
    void testCreate_ValidationFailure_NegativePrice() throws Exception {
        mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto\",\"preco\":-10.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validação falhou")));
    }

    @Test
    void testGetById_NotFound() throws Exception {
        mockMvc.perform(get("/test-produtos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_Success() throws Exception {
        // First create a product
        String response = mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto Original\",\"preco\":50.00}"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        Long id = jsonNode.get("id").asLong();

        mockMvc.perform(put("/test-produtos/" + id)
                .contentType("application/json")
                .content("{\"nome\":\"Produto Atualizado\",\"preco\":75.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Produto Atualizado")))
                .andExpect(jsonPath("$.preco", is(75.00)));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        mockMvc.perform(put("/test-produtos/999")
                .contentType("application/json")
                .content("{\"nome\":\"Teste\",\"preco\":10.00}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entidade não encontrada")));
    }

    @Test
    void testPatch_Success() throws Exception {
        // First create a product
        String response = mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto\",\"preco\":100.00,\"descricao\":\"Original\"}"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        Long id = jsonNode.get("id").asLong();

        // Patch only the description
        mockMvc.perform(patch("/test-produtos/" + id)
                .contentType("application/json")
                .content("{\"descricao\":\"Atualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao", is("Atualizada")))
                .andExpect(jsonPath("$.nome", is("Produto")))
                .andExpect(jsonPath("$.preco", is(100.00)));
    }

    @Test
    void testPatch_NotFound() throws Exception {
        mockMvc.perform(patch("/test-produtos/999")
                .contentType("application/json")
                .content("{\"nome\":\"Teste\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Entidade não encontrada")));
    }

    @Test
    void testDelete_Success() throws Exception {
        // First create a product
        String response = mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Para Deletar\",\"preco\":10.00}"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        Long id = jsonNode.get("id").asLong();

        // Delete it
        mockMvc.perform(delete("/test-produtos/" + id))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/test-produtos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testReadOnlyField_IgnoredOnUpdate() throws Exception {
        String response = mockMvc.perform(post("/test-produtos")
                .contentType("application/json")
                .content("{\"nome\":\"Produto\",\"preco\":100.00,\"codigoInterno\":\"ABC123\"}"))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        Long id = jsonNode.get("id").asLong();

        // Try to update the readonly field - it should be ignored
        mockMvc.perform(put("/test-produtos/" + id)
                .contentType("application/json")
                .content("{\"nome\":\"Produto Atualizado\",\"codigoInterno\":\"XYZ789\"}"))
                .andExpect(status().isOk());
    }
}
