package integration;

import com.rokaidev.fasterapi.annotation.FasterCRUD;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AutoDtoIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "fasterapi.base-package=integration",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AutoDtoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Entity
    @FasterCRUD(path = "/usuarios")
    public static class Usuario {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @NotBlank
        private String nome;
        
        private String email;
        
        @JsonIgnore
        private String senha;  // Não deve ser exposto na API
        
        @JsonIgnore
        private String token;  // Não deve ser exposto na API
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @Test
    void testCreateWithAutoDto() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("{\"nome\":\"João\",\"email\":\"joao@teste.com\",\"senha\":\"123456\",\"token\":\"abc123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("João")))
                .andExpect(jsonPath("$.email", is("joao@teste.com")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.senha").doesNotExist())  // Campo @JsonIgnore não deve aparecer
                .andExpect(jsonPath("$.token").doesNotExist());  // Campo @JsonIgnore não deve aparecer
    }

    @Test
    void testFindByIdWithAutoDto() throws Exception {
        // Criar usuário
        String response = mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("{\"nome\":\"Maria\",\"email\":\"maria@teste.com\",\"senha\":\"senha123\",\"token\":\"xyz789\"}"))
                .andReturn().getResponse().getContentAsString();
        
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Buscar por ID
        mockMvc.perform(get("/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Maria")))
                .andExpect(jsonPath("$.email", is("maria@teste.com")))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void testUpdateWithAutoDto() throws Exception {
        // Criar
        String response = mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("{\"nome\":\"Pedro\",\"email\":\"pedro@teste.com\",\"senha\":\"oldpass\",\"token\":\"oldtoken\"}"))
                .andReturn().getResponse().getContentAsString();
        
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Atualizar
        mockMvc.perform(put("/usuarios/" + id)
                .contentType("application/json")
                .content("{\"nome\":\"Pedro Silva\",\"email\":\"pedro.silva@teste.com\",\"senha\":\"newpass\",\"token\":\"newtoken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Pedro Silva")))
                .andExpect(jsonPath("$.email", is("pedro.silva@teste.com")))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void testListAllWithAutoDto() throws Exception {
        // Criar usuários
        mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("{\"nome\":\"User1\",\"email\":\"user1@teste.com\",\"senha\":\"pass1\",\"token\":\"token1\"}"))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("{\"nome\":\"User2\",\"email\":\"user2@teste.com\",\"senha\":\"pass2\",\"token\":\"token2\"}"))
                .andExpect(status().isCreated());
        
        // Listar todos
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].senha").doesNotExist())
                .andExpect(jsonPath("$[0].token").doesNotExist())
                .andExpect(jsonPath("$[1].senha").doesNotExist())
                .andExpect(jsonPath("$[1].token").doesNotExist());
    }

    @Test
    void testPatchWithAutoDto() throws Exception {
        // Criar
        String response = mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("{\"nome\":\"Ana\",\"email\":\"ana@teste.com\",\"senha\":\"anapass\",\"token\":\"anatoken\"}"))
                .andReturn().getResponse().getContentAsString();
        
        org.json.JSONObject json = new org.json.JSONObject(response);
        Long id = json.getLong("id");
        
        // Patch parcial
        mockMvc.perform(patch("/usuarios/" + id)
                .contentType("application/json")
                .content("{\"email\":\"ana.nova@teste.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Ana")))
                .andExpect(jsonPath("$.email", is("ana.nova@teste.com")))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }
}
