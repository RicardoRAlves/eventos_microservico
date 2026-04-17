package com.br.capoeira.eventos.organization_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String slug;
    @NotNull
    private String description;
    @NotNull
    private String logoUrl;
    @NotNull
    private Boolean active;
    @JsonIgnore
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    @JsonIgnore
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrganizationUnit> units = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        var now = LocalDateTime.now();
        createAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
