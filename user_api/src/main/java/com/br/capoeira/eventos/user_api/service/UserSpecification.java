package com.br.capoeira.eventos.user_api.service;

import com.br.capoeira.eventos.user_api.enums.Role;
import com.br.capoeira.eventos.user_api.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> byFilters(
            Long organizationId,
            Long organizationUnitId,
            Boolean active,
            Role role
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            if (organizationId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("organizationId"), organizationId)
                );
            }

            if (organizationUnitId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("organizationUnitId"), organizationUnitId)
                );
            }

            if (active != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("active"), active)
                );
            }

            if (role != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("role"), role)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
