package com.br.capoeira.eventos.organization_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organization")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Setter(AccessLevel.NONE)
    @Column(
            name = "organization_key",
            nullable = false,
            unique = true,
            length = 120,
            updatable = true
    )
    private String organizationKey;

    @NotNull
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private String logoUrl;

    @NotNull
    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<OrganizationUnit> units = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void assignOrganizationKey(String organizationKey) {
        this.organizationKey = organizationKey;
    }
}
