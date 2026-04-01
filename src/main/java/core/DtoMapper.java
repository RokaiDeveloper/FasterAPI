package core;

import annotation.CrudDTO;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class DtoMapper {

    public static Object converter(Object entidade) {
        if (entidade == null) return null;

        Class<?> classeEntidade = entidade.getClass();
        CrudDTO anotacao = classeEntidade.getAnnotation(CrudDTO.class);

        if (anotacao == null) {
            return converterParaMap(entidade, classeEntidade);
        }

        return converterParaDto(entidade, classeEntidade, anotacao.value());
    }

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
                }
            }

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter para DTO: " + classeDto.getSimpleName(), e);
        }
    }

    private static Map<String, Object> converterParaMap(Object entidade,
                                                        Class<?> classeEntidade) {
        Map<String, Object> mapa = new LinkedHashMap<>();

        for (Field campo : classeEntidade.getDeclaredFields()) {
            try {
                campo.setAccessible(true);
                mapa.put(campo.getName(), campo.get(entidade));
            } catch (IllegalAccessException e) {

            }
        }

        return mapa;
    }
}