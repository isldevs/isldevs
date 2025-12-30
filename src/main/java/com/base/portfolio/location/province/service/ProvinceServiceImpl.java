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
package com.base.portfolio.location.province.service;

import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.location.district.dto.DistrictDTO;
import com.base.portfolio.location.province.dto.ProvinceDTO;
import com.base.portfolio.location.province.model.Province;
import com.base.portfolio.location.province.repository.ProvinceRepository;
import com.base.portfolio.location.province.validation.ProvinceDataValidation;
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
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
public class ProvinceServiceImpl implements ProvinceService {

    private final JdbcTemplate jdbcTemplate;
    private final MessageSource messageSource;
    private final ProvinceRepository repository;
    private final ProvinceDataValidation validation;

    @Autowired
    public ProvinceServiceImpl(final JdbcTemplate jdbcTemplate,
                               final MessageSource messageSource,
                               final ProvinceRepository repository,
                               final ProvinceDataValidation validation) {
        this.jdbcTemplate = jdbcTemplate;
        this.messageSource = messageSource;
        this.repository = repository;
        this.validation = validation;
    }

    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public Map<String, Object> createProvince(JsonCommand command) {
        this.validation.create(command.getJson());

        final var data = Province.fromJson(command);

        this.repository.save(data);
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public Map<String, Object> updateProvince(Long id,
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
    @CacheEvict(value = "provinces", allEntries = true)
    public Map<String, Object> deleteProvince(Long id) {
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
    // @Cacheable(value = "provinces", key = "#id")
    public ProvinceDTO getProvinceById(Long id) {
        try {
            return jdbcTemplate.queryForObject("""
                                               SELECT p.id, p.name_en, p.name_km, p.name_zh, p.type, p.postal_code,
                                                            COALESCE(
                                                               json_agg(
                                                                   json_build_object(
                                                                       'id', d.id,
                                                                       'nameEn', trim(d.name_en),
                                                                       'nameKm', trim(d.name_km),
                                                                       'nameZh', trim(d.name_zh),
                                                                       'type', trim(d.type),
                                                                       'postalCode', trim(d.postal_code)
                                                                   )
                                                               ) FILTER (WHERE d.id IS NOT NULL), '[]'
                                                            ) AS districts
                                                       FROM province p
                                                       LEFT JOIN district d ON p.id = d.province_id
                                                       WHERE p.id = ?
                                                       GROUP BY p.id, p.name_km, p.type, p.postal_code;
                                               """, this::mapRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("msg.not.found", id);
        }
    }

    @Override
    @Cacheable(value = "provinces", key = "#page + '-' + #size + '-' + #search")
    public Page<ProvinceDTO> listProvinces(Integer page,
                                           Integer size,
                                           String search) {

        try {
            StringBuilder sqlBuilder = new StringBuilder("""
                                                         SELECT p.id, p.type, p.name_en, p.name_km, p.name_zh, p.postal_code
                                                         FROM province p
                                                         """);

            StringBuilder countSqlBuilder = new StringBuilder(" SELECT COUNT(*) FROM province p ");

            List<Object> params = new ArrayList<>();
            List<Object> countParams = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                sqlBuilder.append("""
                                  CROSS JOIN LATERAL (
                                      SELECT
                                          similarity(?, p.name_en) as name_en_sim,
                                          similarity(?, p.name_km) as name_km_sim,
                                          similarity(?, p.name_zh) as name_zh_sim,
                                          similarity(?, p.type) as type_sim,
                                          similarity(?, p.postal_code) as postal_sim
                                  ) s
                                  WHERE s.name_en_sim >= ? OR s.name_km_sim >= ? OR s.name_zh_sim >= ? OR s.type_sim >= ? OR s.postal_sim >= ?
                                  """);

                countSqlBuilder.append("""
                                       CROSS JOIN LATERAL (
                                           SELECT
                                               similarity(?, p.name_en) as name_en_sim,
                                               similarity(?, p.name_km) as name_km_sim,
                                               similarity(?, p.name_zh) as name_zh_sim,
                                               similarity(?, p.type) as type_sim,
                                               similarity(?, p.postal_code) as postal_sim
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
            }

            sqlBuilder.append("""
                                  GROUP BY p.id, p.type, p.name_en, p.name_km, p.name_zh, p.postal_code
                                  ORDER BY p.id ASC
                              """);

            final String sql = sqlBuilder.toString();
            final String countSql = countSqlBuilder.toString();

            if (page != null && size != null) {
                String paginatedSql = sql + " LIMIT ? OFFSET ?";
                params.add(size);
                params.add(page * size);

                List<ProvinceDTO> content = jdbcTemplate.query(paginatedSql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(content, pageable, total != null
                        ? total
                        : 0);
            } else {
                List<ProvinceDTO> content = jdbcTemplate.query(sql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                return new PageImpl<>(content, Pageable.unpaged(), total != null
                        ? total
                        : 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorException("msg.internal.error", "Error fetching province list", e);
        }
    }

    private ProvinceDTO mapRow(ResultSet rs,
                               int rowNum) throws SQLException {
        final Long id = rs.getLong("id");
        final String nameEn = rs.getString("name_en");
        final String nameKm = rs.getString("name_km");
        final String nameZh = rs.getString("name_zh");
        final String type = rs.getString("type");
        final String postalCode = rs.getString("postal_code");
        List<DistrictDTO> districts = null;
        try {
            districts = fromJsonAsList(rs.getString("districts"), DistrictDTO[].class);
        } catch (SQLException ignored) {
        }
        return ProvinceDTO.builder()
                .id(id)
                .type(type)
                .nameEn(nameEn)
                .nameKm(nameKm)
                .nameZh(nameZh)
                .postalCode(postalCode)
                .districts(districts)
                .build();
    }

    public static <T> List<T> fromJsonAsList(String json,
                                             Class<T[]> className) {
        return json != null
                ? Arrays.asList(new Gson().fromJson(json, className))
                : new ArrayList<>();
    }

}
