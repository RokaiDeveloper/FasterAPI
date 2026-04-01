package core;

import annotation.CrudDTO;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilitario responsavel por converter entidades em DTOs ou Maps.
 * Esta classe usa reflexao para mapear campos entre entidades e DTOs
 * de forma automatica, facilitando a transformacao de dados.
 * 
 * @author FasterAPI Framework
 * @since 1.0.0
 */
public class DtoMapper {

    /**
     * Converte uma entidade para DTO ou Map dependendo da configuracao.
     * Se a entidade tiver a anotacao @CrudDTO, converte para o DTO especificado.
     * Caso contrario, converte para um Map com os campos da entidade.
     * 
     * @param entidade entidade a ser convertida
     * @return DTO ou Map com os dados da entidade
     */
    public static Object converter(Object entidade) {
        if (entidade == null) return null;

        Class<?> classeEntidade = entidade.getClass();
        CrudDTO anotacao = classeEntidade.getAnnotation(CrudDTO.class);

        if (anotacao == null) {
            return converterParaMap(entidade, classeEntidade);
        }

        return converterParaDto(entidade, classeEntidade, anotacao.value());
    }

    /**
     * Converte uma entidade para um DTO especifico usando reflexao.
     * Mapeia os campos que possuem o mesmo nome na entidade e no DTO.
     * 
     * @param entidade entidade origem
     * @param classeEntidade classe da entidade
     * @param classeDto classe do DTO destino
     * @return instancia do DTO preenchida
     */
    private static Object converterParaDto(Object entidade,
                                           Class<?> classeEntidade,
                                           Class<?> classeDto) {
        try {
            Object dto = classeDto.getDeclaredConstructor().newInstance();

            for (Field campoDto : classeDto.getDeclaredFields()) {
                try {
                    Field campoEntidade = classeEntidade.getDeclaredField(campoDto.getName());
                    campoEntidade.setAccessible(true);
                    campoDto.setAccessible(true);

                    Object valor = campoEntidade.get(entidade);
                    campoDto.set(dto, valor);

                } catch (NoSuchFieldException e) {
                    // Campo nao existe na entidade, ignora
                }
            }

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter para DTO: " + classeDto.getSimpleName(), e);
        }
    }

    /**
     * Converte uma entidade para um Map com todos os seus campos.
     * O Map mantem a ordem dos campos da entidade usando LinkedHashMap.
     * 
     * @param entidade entidade a ser convertida
     * @param classeEntidade classe da entidade
     * @return Map com nome e valor dos campos
     */
    private static Map<String, Object> converterParaMap(Object entidade,
                                                        Class<?> classeEntidade) {
        Map<String, Object> mapa = new LinkedHashMap<>();

        for (Field campo : classeEntidade.getDeclaredFields()) {
            try {
                campo.setAccessible(true);
                mapa.put(campo.getName(), campo.get(entidade));
            } catch (IllegalAccessException e) {
                // Nao consegue acessar o campo, ignora
            }
        }

        return mapa;
    }
}