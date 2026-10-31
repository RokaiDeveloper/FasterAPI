package com.rokaidev.fasterapi.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DtoMapperTest {

    private DtoMapper dtoMapper;

    @BeforeEach
    void setUp() {
        dtoMapper = new DtoMapper();
    }

    @Test
    void testSpringContextInitializationWithoutObjectMapperBean() {
        // Valida que o DtoMapper inicializa no Spring context sem exigir ObjectMapper ou EntityManager
        new ApplicationContextRunner()
                .withUserConfiguration(DtoMapper.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(DtoMapper.class);
                    assertThat(context).doesNotHaveBean("objectMapper");
                });
    }

    @Test
    void testToEntity() {
        SampleDto dto = new SampleDto();
        dto.setId(10L);
        dto.setNome("Produto Teste");
        dto.setPreco(new BigDecimal("99.90"));

        SampleEntity entity = dtoMapper.toEntity(dto, SampleEntity.class);

        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals("Produto Teste", entity.getNome());
        assertEquals(new BigDecimal("99.90"), entity.getPreco());
    }

    @Test
    void testToDto() {
        SampleEntity entity = new SampleEntity();
        entity.setId(20L);
        entity.setNome("Entidade Original");
        entity.setPreco(new BigDecimal("150.00"));
        entity.setSenhaSecreta("segredo123");

        SampleDto dto = dtoMapper.toDto(entity, SampleDto.class);

        assertNotNull(dto);
        assertEquals(20L, dto.getId());
        assertEquals("Entidade Original", dto.getNome());
        assertEquals(new BigDecimal("150.00"), dto.getPreco());
    }

    @Test
    void testToFilteredDtoRespectsJsonIgnore() {
        SampleEntity entity = new SampleEntity();
        entity.setId(30L);
        entity.setNome("Item Filtrado");
        entity.setPreco(new BigDecimal("50.00"));
        entity.setSenhaSecreta("nao-deve-aparecer");

        var filteredMap = dtoMapper.toFilteredDto(entity);

        assertNotNull(filteredMap);
        assertEquals(30L, filteredMap.get("id"));
        assertEquals("Item Filtrado", filteredMap.get("nome"));
        assertEquals(new BigDecimal("50.00"), filteredMap.get("preco"));
        assertFalse(filteredMap.containsKey("senhaSecreta"), "Campo com @JsonIgnore não deve ser exportado");
    }

    @Test
    void testUpdateEntity() {
        SampleEntity entity = new SampleEntity();
        entity.setId(1L);
        entity.setNome("Nome Antigo");
        entity.setPreco(new BigDecimal("10.00"));

        SampleDto dto = new SampleDto();
        dto.setId(1L);
        dto.setNome("Nome Atualizado");
        dto.setPreco(new BigDecimal("25.00"));

        dtoMapper.updateEntity(dto, entity);

        assertEquals("Nome Atualizado", entity.getNome());
        assertEquals(new BigDecimal("25.00"), entity.getPreco());
    }

    public static class SampleEntity {
        private Long id;
        private String nome;
        private BigDecimal preco;

        @JsonIgnore
        private String senhaSecreta;

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

        public String getSenhaSecreta() {
            return senhaSecreta;
        }

        public void setSenhaSecreta(String senhaSecreta) {
            this.senhaSecreta = senhaSecreta;
        }
    }

    public static class SampleDto {
        private Long id;
        private String nome;
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
