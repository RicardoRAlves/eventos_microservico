package com.br.capoeira.eventos.processor_api.mapper;

import com.br.capoeira.eventos.processor_api.dto.EventSaleItemRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventSaleItemResponseDto;
import com.br.capoeira.eventos.processor_api.entities.EventSaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventSaleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventSaleItem requestDtoToEventSaleItem(
            EventSaleItemRequestDto input
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateRequestDtoToEventSaleItem(
            EventSaleItemRequestDto input,
            @MappingTarget EventSaleItem eventSale
    );

    EventSaleItemResponseDto eventSaleItemToResponseDto(
            EventSaleItem eventSaleItem
    );
}
