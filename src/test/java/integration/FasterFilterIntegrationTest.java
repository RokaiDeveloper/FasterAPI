package integration;

import com.rokaidev.fasterapi.annotation.FasterFilter;
import com.rokaidev.fasterapi.autoconfigure.FasterCrudInitializer;
import com.rokaidev.fasterapi.autoconfigure.FasterFilterAutoConfiguration;
import com.rokaidev.fasterapi.persistence.FilterValidationException;
import com.rokaidev.fasterapi.web.FasterFilters;
import com.rokaidev.fasterapi.web.FasterFilterQuery;
import com.rokaidev.fasterapi.web.FilterExceptionHandler;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootApplication
@org.springframework.context.annotation.ComponentScan(basePackages = "integration")
@org.springframework.context.annotation.Import({FasterFilterAutoConfiguration.class, FilterExceptionHandler.class})
class FilterTestApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(FilterTestApplication.class, args);
    }
}

@SpringBootTest(classes = FilterTestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class FasterFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomProdutoController customProdutoController;

    @Entity
    public static class TestProduto {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String nome;

        private BigDecimal preco;

        private String descricao;

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
    }

    @RestController
    @RequestMapping("/custom-produtos")
    @org.springframework.stereotype.Component
    public static class CustomProdutoController {

        private final SimpleJpaRepository<TestProduto, Long> repository;

        public CustomProdutoController(EntityManager entityManager) {
            JpaEntityInformation<TestProduto, ?> info = 
                JpaEntityInformationSupport.getEntityInformation(TestProduto.class, entityManager);
            this.repository = new SimpleJpaRepository<>(info, entityManager);
        }

        @GetMapping
        public Object list(@FasterFilter(entity = TestProduto.class, fields = {"nome", "preco"}) FasterFilters<TestProduto> filters) {
            return repository.findAll(filters.getSpecification());
        }

        @GetMapping("/simplified")
        public Object simplifiedList(@FasterFilter(
                entity = TestProduto.class,
                fields = {"nome", "preco"}
        ) FasterFilterQuery<TestProduto> query) {
            return query.findAll();
        }
    }

    @BeforeEach
    public void setup() {
        // Insert test data
        TestProduto p1 = new TestProduto();
        p1.setNome("Notebook");
        p1.setPreco(new BigDecimal("5000"));
        p1.setDescricao("Dell");
        entityManager.persist(p1);

        TestProduto p2 = new TestProduto();
        p2.setNome("Mouse");
        p2.setPreco(new BigDecimal("50"));
        p2.setDescricao("Logitech");
        entityManager.persist(p2);

        entityManager.flush();
    }

    @Test
    public void testFilterWithValidField() throws Exception {
        mockMvc.perform(get("/custom-produtos?nome=Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Notebook"));
    }

    @Test
    public void testFilterWithLikeOperator() throws Exception {
        mockMvc.perform(get("/custom-produtos?nome__like=ote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Notebook"));
    }

    @Test
    public void testFilterWithNumericOperator() throws Exception {
        mockMvc.perform(get("/custom-produtos?preco__gt=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Notebook"));
    }

    @Test
    public void testFilterWithLessThanOperator() throws Exception {
        mockMvc.perform(get("/custom-produtos?preco__lt=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Mouse"));
    }

    @Test
    public void testFilterWithGreaterThanOrEqualOperator() throws Exception {
        mockMvc.perform(get("/custom-produtos?preco__gte=5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Notebook"));
    }

    @Test
    public void testFilterWithLessThanOrEqualOperator() throws Exception {
        mockMvc.perform(get("/custom-produtos?preco__lte=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Mouse"));
    }

    @Test
    public void testPaginationAndSortParametersAreIgnored() throws Exception {
        mockMvc.perform(get("/custom-produtos?page=0&size=1&sort=preco,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testFilterWithInvalidFieldStrict() throws Exception {
        mockMvc.perform(get("/custom-produtos?descricao=Dell"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Campo 'descricao' não é permitido")));
    }

    @Test
    public void testFilterWithNonExistentFieldStrict() throws Exception {
        mockMvc.perform(get("/custom-produtos?campoInexistente=valor"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Campo 'campoInexistente' não é permitido")));
    }

    @Test
    public void testFilterWithInvalidOperatorStrict() throws Exception {
        mockMvc.perform(get("/custom-produtos?nome__invalid=valor"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Operador 'invalid' não suportado")));
    }

    @Test
    public void testFilterWithLikeOnNumericFieldStrict() throws Exception {
        mockMvc.perform(get("/custom-produtos?preco__like=5000"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Operador 'like' só pode ser usado em campos do tipo String")));
    }

    @Test
    public void testFilterWithGtOnStringFieldStrict() throws Exception {
        mockMvc.perform(get("/custom-produtos?nome__gt=A"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Operador 'gt' só pode ser usado em campos numéricos")));
    }

    @Test
    public void testFilterWithInvalidNumericValueStrict() throws Exception {
        mockMvc.perform(get("/custom-produtos?preco__gt=not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Valor 'not-a-number' inválido para o campo 'preco'")));
    }

    @Test
    public void testNoFilterReturnsAll() throws Exception {
        mockMvc.perform(get("/custom-produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testSimplifiedFilterQueryReturnsFilteredResults() throws Exception {
        mockMvc.perform(get("/custom-produtos/simplified?preco__lt=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Mouse"));
    }
}
