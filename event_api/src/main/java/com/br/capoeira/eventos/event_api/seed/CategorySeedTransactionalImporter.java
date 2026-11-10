package com.br.capoeira.eventos.event_api.seed;

import com.br.capoeira.eventos.event_api.config.exception.CategorySeedException;
import com.br.capoeira.eventos.event_api.dto.CategoryCreateRequestDto;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategorySeedTransactionalImporter {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Transactional(transactionManager = "mongoTransactionManager")
    public int importIfEmpty(List<CategorySeedItem> categories) {
        try {
            if (categoryRepository.count() > 0) {
                return 0;
            }

            categories.forEach(item -> categoryService.create(new CategoryCreateRequestDto(item.getName())));
            return categories.size();
        } catch (DataAccessException exception) {
            throw new CategorySeedException(
                    "Transactional category seed failed. MongoDB must be a replica set or sharded cluster; no non-transactional fallback was attempted.",
                    exception
            );
        }
    }
}
