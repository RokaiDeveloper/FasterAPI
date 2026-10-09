package integration;

import annotation.EntityMapping;
import annotation.FasterCRUD;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DtoIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "fasterapi.base-package=integration",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DtoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Entity
    public static class Produto {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @NotBlank
        private String nome;
        
        private String descricao;
        
        private Double preco;
        
        // Campo interno não exposto
        private String codigoInterno;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public Double getPreco() { return preco; }
        public void setPreco(Double preco) { this.preco = preco; }
        public String getCodigoInterno() { return codigoInterno; }
        public void setCodigoInterno(String codigoInterno) { this.codigoInterno = codigoInterno; }
    }

    @FasterCRUD(path = "/produtos-dto", isDto = true)
    @EntityMapping(entity = Produto.class)
    public static class ProdutoDTO {
        private Long id;
        private String nome;
        private String descricao;
        private Double preco;
        
        // Não inclui codigoInterno - campo sensível
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public Double getPreco() { return preco; }
        public void setPreco(Double preco) { this.preco = preco; }
    }

    @SpringBootApplication
    @ComponentScan(basePackages = {"annotation", "core", "integration"})
    static class TestApplication {
    }

    @Test
    void testCreateViaDto() throws Exception {
        mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Notebook\",\"descricao\":\"Notebook Dell\",\"preco\":3500.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Notebook")))
                .andExpect(jsonPath("$.descricao", is("Notebook Dell")))
                .andExpect(jsonPath("$.preco", is(3500.00)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void testFindByIdViaDto() throws Exception {
        // Criar primeiro
        String response = mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Mouse\",\"descricao\":\"Mouse Logitech\",\"preco\":50.00}"))
                .andReturn().getResponse().getContentAsString();
        
        // Extrair ID
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Buscar por ID
        mockMvc.perform(get("/produtos-dto/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Mouse")))
                .andExpect(jsonPath("$.descricao", is("Mouse Logitech")))
                .andExpect(jsonPath("$.preco", is(50.00)));
    }

    @Test
    void testUpdateViaDto() throws Exception {
        // Criar
        String response = mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Teclado\",\"descricao\":\"Teclado básico\",\"preco\":100.00}"))
                .andReturn().getResponse().getContentAsString();
        
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Atualizar
        mockMvc.perform(put("/produtos-dto/" + id)
                .contentType("application/json")
                .content("{\"nome\":\"Teclado Gamer\",\"descricao\":\"Teclado RGB\",\"preco\":200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Teclado Gamer")))
                .andExpect(jsonPath("$.descricao", is("Teclado RGB")))
                .andExpect(jsonPath("$.preco", is(200.00)));
    }

    @Test
    void testPatchViaDto() throws Exception {
        // Criar
        String response = mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Monitor\",\"descricao\":\"Monitor 24 polegadas\",\"preco\":800.00}"))
                .andReturn().getResponse().getContentAsString();
        
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Patch parcial
        mockMvc.perform(patch("/produtos-dto/" + id)
                .contentType("application/json")
                .content("{\"preco\":750.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Monitor")))
                .andExpect(jsonPath("$.preco", is(750.00)));
    }

    @Test
    void testListAllViaDto() throws Exception {
        // Criar alguns produtos
        mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Produto 1\",\"descricao\":\"Desc 1\",\"preco\":100.00}"))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Produto 2\",\"descricao\":\"Desc 2\",\"preco\":200.00}"))
                .andExpect(status().isCreated());
        
        // Listar todos
        mockMvc.perform(get("/produtos-dto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testDeleteViaDto() throws Exception {
        // Criar
        String response = mockMvc.perform(post("/produtos-dto")
                .contentType("application/json")
                .content("{\"nome\":\"Temp\",\"descricao\":\"Temporário\",\"preco\":10.00}"))
                .andReturn().getResponse().getContentAsString();
        
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Deletar
        mockMvc.perform(delete("/produtos-dto/" + id))
                .andExpect(status().isNoContent());
        
        // Verificar que foi deletado
        mockMvc.perform(get("/produtos-dto/" + id))
                .andExpect(status().isNotFound());
    }
}
