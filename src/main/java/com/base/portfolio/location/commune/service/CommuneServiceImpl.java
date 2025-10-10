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
package com.base.portfolio.location.commune.service;

import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.location.commune.controller.CommuneConstants;
import com.base.portfolio.location.commune.dto.CommuneDTO;
import com.base.portfolio.location.commune.model.Commune;
import com.base.portfolio.location.commune.repository.CommuneRepository;
import com.base.portfolio.location.commune.validation.CommuneDataValidation;
import com.base.portfolio.location.district.model.District;
import com.base.portfolio.location.district.repository.DistrictRepository;
import com.base.portfolio.location.village.dto.VillageDTO;
import com.google.gson.Gson;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class CommuneServiceImpl implements CommuneService {

    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final CommuneRepository repository;
    private final CommuneDataValidation validation;
    private final DistrictRepository districtRepository;

    @Autowired
    public CommuneServiceImpl(final MessageSource messageSource,
                              final JdbcTemplate jdbcTemplate,
                              final CommuneRepository repository,
                              final CommuneDataValidation validation,
                              final DistrictRepository districtRepository) {
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
        this.validation = validation;
        this.districtRepository = districtRepository;
    }

    @Override
    @CacheEvict(value = "communes", allEntries = true)
    public Map<String, Object> createCommune(JsonCommand command) {
        this.validation.create(command.getJson());

        final Long districtId = command.extractLong(CommuneConstants.DISTRICT);
        final District district = this.districtRepository.findById(districtId)
                .orElseThrow(() -> new NotFoundException(CommuneConstants.DISTRICT));

        final var data = Commune.fromJson(district, command);

        this.repository.save(data);
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "communes", key = "#id")
    public Map<String, Object> updateCommune(Long id,
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
    @CacheEvict(value = "communes", key = "#id")
    public Map<String, Object> deleteCommune(Long id) {
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
    @Cacheable(value = "communes", key = "#id")
    public CommuneDTO getCommuneById(Long id) {
        try {
            return jdbcTemplate.queryForObject("""

                                                SELECT c.id, c.district_id, c.name_en, c.name_km, c.name_zh, c.type, c.postal_code,
                                               COALESCE(
                                                               json_agg(
                                                                   json_build_object(
                                                                           'id', v.id,
                                                                           'nameEn', v.name_en,
                                                                           'nameKm', v.name_km,
                                                                           'nameZh', v.name_zh,
                                                                           'type', v.type,
                                                                           'postalCode', v.postal_code
                                                                   )
                                                              ) FILTER (WHERE v.id IS NOT NULL), '[]'
                                               ) AS villages
                                               FROM commune c
                                               LEFT JOIN village v ON c.id = v.commune_id
                                               WHERE c.id = ?
                                               GROUP BY c.id, c.name_km, c.type, c.postal_code
                                               """, this::mapRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("msg.not.found", id);
        }
    }

    @Override
    @Cacheable(value = "communes", key = "#page + '-' + #size + '-' + #search")
    public Page<CommuneDTO> listCommunes(Integer page,
                                         Integer size,
                                         String search) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("""
                                                         SELECT c.id, c.district_id, c.type, c.name_en, c.name_km, c.name_zh, c.postal_code
                                                         """);

            StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM commune c ");

            List<Object> params = new ArrayList<>();
            List<Object> countParams = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                sqlBuilder.append("""
                                  , GREATEST(s.name_en_sim, s.name_km_sim, s.name_zh_sim, s.type_sim, s.postal_sim) AS max_similarity
                                  FROM commune c
                                  CROSS JOIN LATERAL (
                                      SELECT
                                          similarity(?, c.name_en)  AS name_en_sim,
                                          similarity(?, c.name_km)  AS name_km_sim,
                                          similarity(?, c.name_zh)  AS name_zh_sim,
                                          similarity(?, c.type)     AS type_sim,
                                          similarity(?, c.postal_code) AS postal_sim
                                  ) s
                                  WHERE s.name_en_sim >= ? OR s.name_km_sim >= ? OR s.name_zh_sim >= ? OR s.type_sim >= ? OR s.postal_sim >= ?
                                  ORDER BY max_similarity DESC
                                  """);

                countSqlBuilder.append("""
                                       CROSS JOIN LATERAL (
                                           SELECT
                                               similarity(?, c.name_en)  AS name_en_sim,
                                               similarity(?, c.name_km)  AS name_km_sim,
                                               similarity(?, c.name_zh)  AS name_zh_sim,
                                               similarity(?, c.type)     AS type_sim,
                                               similarity(?, c.postal_code) AS postal_sim
                                       ) s
                                       WHERE s.name_en_sim >= ? OR s.name_km_sim >= ? OR s.name_zh_sim >= ? OR s.type_sim >= ? OR s.postal_sim >= ?
                                       """);

                double threshold = 0.2;
                IntStream.range(0, 5)
                        .forEach(_ -> {
                            params.add(search);
                            countParams.add(search);
                        });

                IntStream.range(0, 5)
                        .forEach(_ -> {
                            params.add(threshold);
                            countParams.add(threshold);
                        });

            } else {
                sqlBuilder.append("""
                                      FROM commune c
                                      ORDER BY c.id ASC
                                  """);
            }

            final String sql = sqlBuilder.toString();
            final String countSql = countSqlBuilder.toString();

            if (page != null && size != null) {
                String paginatedSql = sql + " LIMIT ? OFFSET ?";
                params.add(size);
                params.add(page * size);

                List<CommuneDTO> content = jdbcTemplate.query(paginatedSql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(content, pageable, total != null
                        ? total
                        : 0);
            } else {
                List<CommuneDTO> content = jdbcTemplate.query(sql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                return new PageImpl<>(content, Pageable.unpaged(), total != null
                        ? total
                        : 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorException("msg.internal.error", "Error fetching communes list", e);
        }
    }

    private CommuneDTO mapRow(ResultSet rs,
                              int rowNum) throws SQLException {

        final Long id = rs.getLong("id");
        final Long districtId = rs.getLong("district_id");
        final String type = rs.getString("type");
        final String postalCode = rs.getString("postal_code");
        final String nameEn = rs.getString("name_en");
        final String nameKm = rs.getString("name_km");
        final String nameZh = rs.getString("name_zh");
        List<VillageDTO> villages = null;
        try {
            villages = fromJsonAsList(rs.getString("villages"), VillageDTO[].class);
        } catch (SQLException ignored) {
        }

        return CommuneDTO.builder()
                .id(id)
                .districtId(districtId)
                .type(type)
                .postalCode(postalCode)
                .nameEn(nameEn)
                .nameKm(nameKm)
                .nameZh(nameZh)
                .villages(villages)
                .build();
    }

    public static <T> List<T> fromJsonAsList(String json,
                                             Class<T[]> className) {
        return json != null
                ? Arrays.asList(new Gson().fromJson(json, className))
                : new ArrayList<>();
    }

}
