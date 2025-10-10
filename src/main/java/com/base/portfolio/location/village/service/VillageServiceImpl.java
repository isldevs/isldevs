/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.portfolio.location.village.service;

import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.location.commune.model.Commune;
import com.base.portfolio.location.commune.repository.CommuneRepository;
import com.base.portfolio.location.village.controller.VillageConstants;
import com.base.portfolio.location.village.dto.VillageDTO;
import com.base.portfolio.location.village.model.Village;
import com.base.portfolio.location.village.repository.VillageRepository;
import com.base.portfolio.location.village.validation.VillageDataValidation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
public class VillageServiceImpl implements VillageService {

    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final VillageRepository repository;
    private final VillageDataValidation validation;
    private final CommuneRepository communeRepository;

    @Autowired
    public VillageServiceImpl(final MessageSource messageSource,
                              final JdbcTemplate jdbcTemplate,
                              final VillageRepository repository,
                              final VillageDataValidation validation,
                              final CommuneRepository communeRepository) {
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
        this.validation = validation;
        this.communeRepository = communeRepository;
    }

    @Override
    @CacheEvict(value = "villages", allEntries = true)
    public Map<String, Object> createVillage(JsonCommand command) {
        this.validation.create(command.getJson());

        final Long communeId = command.extractLong(VillageConstants.COMMUNE);
        final Commune commune = this.communeRepository.findById(communeId)
                .orElseThrow(() -> new NotFoundException(VillageConstants.COMMUNE));

        final var data = Village.fromJson(commune, command);

        this.repository.save(data);
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "villages", key = "#id")
    public Map<String, Object> updateVillage(Long id,
                                             JsonCommand command) {
        var data = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));

        this.validation.update(command.getJson());

        var changes = data.changed(command);
        if (!changes.isEmpty()) {
            this.repository.save(data);
        }
        return LogData.builder()
                .id(data.getId())
                .changes(changes)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "villages", key = "#id")
    public Map<String, Object> deleteVillage(Long id) {
        final var data = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));
        this.repository.delete(data);
        this.repository.flush();
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @Cacheable(value = "villages", key = "#id")
    public VillageDTO getVillageById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM village WHERE id = ?", this::mapRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("msg.not.found", id);
        }
    }

    @Override
    @Cacheable(value = "villages", key = "#page + '-' + #size + '-' + #search")
    public Page<VillageDTO> listVillages(Integer page,
                                         Integer size,
                                         String search) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("""
                                                             SELECT v.id, v.commune_id, v.name_en, v.name_km, v.name_zh, v.postal_code
                                                         """);

            StringBuilder countSqlBuilder = new StringBuilder(" SELECT COUNT(*) FROM village v ");

            List<Object> params = new ArrayList<>();
            List<Object> countParams = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                sqlBuilder.append("""
                                      , GREATEST(s.name_en_sim, s.name_km_sim, s.name_zh_sim, s.type_sim, s.postal_sim) AS max_similarity
                                      FROM village v
                                      CROSS JOIN LATERAL (
                                          SELECT
                                              similarity(?, v.name_en) as name_en_sim,
                                              similarity(?, v.name_km) as name_km_sim,
                                              similarity(?, v.name_zh) as name_zh_sim,
                                              similarity(?, v.postal_code) as postal_sim
                                      ) s
                                      WHERE s.name_en_sim >= ? OR s.name_km_sim >= ? OR s.name_zh_sim >= ? OR s.postal_sim >= ?
                                      ORDER BY max_similarity DESC
                                  """);

                countSqlBuilder.append("""
                                           CROSS JOIN LATERAL (
                                               SELECT
                                                   similarity(?, v.name_en) as name_en_sim,
                                                   similarity(?, v.name_km) as name_km_sim,
                                                   similarity(?, v.name_zh) as name_zh_sim,
                                                   similarity(?, v.postal_code) as postal_sim
                                           ) s
                                           WHERE s.name_en_sim >= ? OR s.name_km_sim >= ? OR s.name_zh_sim >= ? OR s.postal_sim >= ?
                                       """);

                double threshold = 0.2;
                IntStream.range(0, 3)
                        .forEach(_ -> {
                            params.add(search);
                            countParams.add(search);
                        });
                IntStream.range(0, 3)
                        .forEach(_ -> {
                            params.add(threshold);
                            countParams.add(threshold);
                        });

            } else {
                sqlBuilder.append("""
                                      FROM village v
                                      ORDER BY v.id ASC
                                  """);
            }

            final String sql = sqlBuilder.toString();
            final String countSql = countSqlBuilder.toString();

            if (page != null && size != null) {
                String paginatedSql = sql + " LIMIT ? OFFSET ?";
                params.add(size);
                params.add(page * size);

                List<VillageDTO> content = jdbcTemplate.query(paginatedSql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(content, pageable, total != null
                        ? total
                        : 0);
            } else {
                List<VillageDTO> content = jdbcTemplate.query(sql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                return new PageImpl<>(content, Pageable.unpaged(), total != null
                        ? total
                        : 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorException("msg.internal.error", "Error fetching villages list", e);
        }
    }

    private VillageDTO mapRow(ResultSet rs,
                              int rowNum) throws SQLException {
        return VillageDTO.builder()
                .id(rs.getLong("id"))
                .communeId(rs.getLong("commune_id"))
                .nameEn(rs.getString("name_en"))
                .nameKm(rs.getString("name_km"))
                .nameZh(rs.getString("name_zh"))
                .postalCode(rs.getString("postal_code"))
                .build();
    }

}
