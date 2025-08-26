package com.base.entity.office.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.authentication.user.model.User;
import com.base.core.command.data.JsonCommand;
import com.base.entity.office.controller.OfficeConstants;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "office", uniqueConstraints = {
        @UniqueConstraint(name = "office_name_en_key", columnNames = "name_en"),
        @UniqueConstraint(name = "office_name_km_key", columnNames = "name_km"),
        @UniqueConstraint(name = "office_name_zh_key", columnNames = "name_zh")
})
public class Office extends CustomAbstractAuditable<User> {

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Office parent;

    @Column(name = "hierarchy")
    private String hierarchy;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_km")
    private String nameKm;

    @Column(name = "name_zh")
    private String nameZh;

    protected Office() {
    }

    public Office(Builder builder) {
        this.parent = builder.parent;
        this.hierarchy = builder.hierarchy;
        this.nameEn = builder.nameEn;
        this.nameKm = builder.nameKm;
        this.nameZh = builder.nameZh;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void generateHierarchy() {
        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public Map<String, Object> changed(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsLong(OfficeConstants.PARENT_ID, this.parent == null ? null : this.parent.getId())) {
            final var value = command.extractLong(OfficeConstants.PARENT_ID);
            changes.put(OfficeConstants.PARENT_ID, value);
        }
        if (command.isChangeAsString(OfficeConstants.NAME_EN, this.nameEn)) {
            final var value = command.extractString(OfficeConstants.NAME_EN);
            this.nameEn = value;
            changes.put(OfficeConstants.NAME_EN, value);
        }
        if (command.isChangeAsString(OfficeConstants.NAME_KM, this.nameKm)) {
            final var value = command.extractString(OfficeConstants.NAME_KM);
            this.nameKm = value;
            changes.put(OfficeConstants.NAME_KM, value);
        }
        if (command.isChangeAsString(OfficeConstants.NAME_ZH, this.nameZh)) {
            final var value = command.extractString(OfficeConstants.NAME_ZH);
            this.nameZh = value;
            changes.put(OfficeConstants.NAME_ZH, value);
        }

        return changes;

    }

    public static class Builder {

        private Office parent;
        private String hierarchy;
        private String nameEn;
        private String nameKm;
        private String nameZh;

        public Office build() {
            return new Office(this);
        }

        public Builder parent(Office parent) {
            this.parent = parent;
            return this;
        }

        public Builder hierarchy(String hierarchy) {
            this.hierarchy = hierarchy;
            return this;
        }

        public Builder nameEn(String nameEn) {
            this.nameEn = nameEn;
            return this;
        }

        public Builder nameKm(String nameKm) {
            this.nameKm = nameKm;
            return this;
        }

        public Builder nameZh(String nameZh) {
            this.nameZh = nameZh;
            return this;
        }
    }

    public Office getParent() {
        return parent;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameKm() {
        return nameKm;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setParent(Office parent) {
        this.parent = parent;
    }
}
