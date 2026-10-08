package core;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GenericCrudServiceTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidation_Success() {
        TestEntity entity = new TestEntity();
        entity.setNome("Teste");
        entity.setPreco(new BigDecimal("100.00"));
        
        var violations = validator.validate(entity);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_EmptyName() {
        TestEntity entity = new TestEntity();
        entity.setNome("");
        entity.setPreco(new BigDecimal("100.00"));
        
        var violations = validator.validate(entity);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Nome")));
    }

    @Test
    void testValidation_NullPrice() {
        TestEntity entity = new TestEntity();
        entity.setNome("Teste");
        entity.setPreco(null);
        
        var violations = validator.validate(entity);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Preço")));
    }

    @Test
    void testValidation_NegativePrice() {
        TestEntity entity = new TestEntity();
        entity.setNome("Teste");
        entity.setPreco(new BigDecimal("-10.00"));
        
        var violations = validator.validate(entity);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("positivo")));
    }

    @jakarta.persistence.Entity
    public static class TestEntity {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
        private Long id;

        @jakarta.validation.constraints.NotBlank(message = "Nome é obrigatório")
        private String nome;

        @jakarta.validation.constraints.NotNull(message = "Preço é obrigatório")
        @jakarta.validation.constraints.Positive(message = "Preço deve ser positivo")
        private BigDecimal preco;

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
    }
}
