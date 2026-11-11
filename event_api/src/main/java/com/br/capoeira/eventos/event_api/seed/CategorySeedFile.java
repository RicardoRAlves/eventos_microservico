package com.br.capoeira.eventos.event_api.seed;

import java.util.List;

public class CategorySeedFile {

    private Integer schemaVersion;
    private List<CategorySeedItem> categories;

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<CategorySeedItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategorySeedItem> categories) {
        this.categories = categories;
    }
}
