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
package com.base.portfolio.location.district.service;


import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.location.commune.dto.CommuneDTO;
import com.base.portfolio.location.district.controller.DistrictConstants;
import com.base.portfolio.location.district.dto.DistrictDTO;
import com.base.portfolio.location.district.model.District;
import com.base.portfolio.location.district.repository.DistrictRepository;
import com.base.portfolio.location.district.validation.DistrictDataValidation;
import com.base.portfolio.location.province.model.Province;
import com.base.portfolio.location.province.repository.ProvinceRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author YISivlay
 */
@Service
public class DistrictServiceImpl implements DistrictService {

    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final DistrictRepository repository;
    private final DistrictDataValidation validation;
    private final ProvinceRepository provinceRepository;

    @Autowired
    public DistrictServiceImpl(final MessageSource messageSource,
                               final JdbcTemplate jdbcTemplate,
                               final DistrictRepository repository,
                               final DistrictDataValidation validation,
                               final ProvinceRepository provinceRepository) {
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
        this.validation = validation;
        this.provinceRepository = provinceRepository;
    }


    @Override
    public Map<String, Object> createDistrict(JsonCommand command) {
        this.validation.create(command.getJson());

        final Long provinceId = command.extractLong(DistrictConstants.PROVINCE);
        final Province province = this.provinceRepository.findById(provinceId)
                .orElseThrow(() -> new NotFoundException("msg.not.found", provinceId));

        final var data = District.fromJson(province, command);

        this.repository.save(data);
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build().claims();
    }

    @Override
    public Map<String, Object> updateDistrict(Long id, JsonCommand command) {
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
    public Map<String, Object> deleteDistrict(Long id) {
        final var data = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));
        this.repository.delete(data);
        this.repository.flush();
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build().claims();
    }

    @Override
    public DistrictDTO getDistrictById(Long id) {
        try {
            return jdbcTemplate.queryForObject("""
                            SELECT d.id,
                                                 d.province_id,
                                                 d.name,
                                                 d.type,
                                                 d.postal_code,
                                                 COALESCE(
                                                                 json_agg(
                                                                 json_build_object(
                                                                         'id', c.id,
                                                                         'name', c.name,
                                                                         'type', c.type,
                                                                         'postalCode', c.postal_code
                                                                 )
                                                                         ) FILTER (WHERE c.id IS NOT NULL), '[]'
                                                 ) AS communes
                                          FROM district d
                                                   LEFT JOIN commune c ON d.id = c.district_id
                                          WHERE d.id = ?
                                          GROUP BY d.id, d.name, d.type, d.postal_code;
                            """,
                    this::mapRow, id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("msg.not.found", id);
        }
    }

    @Override
    public Page<DistrictDTO> listDistricts(Integer page, Integer size, String search) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("""
                    SELECT d.id, d.province_id, d.type, d.name, d.postal_code,
                        p.name as province_name, p.type as province_type, p.postal_code as province_postal_code
                    """);

            StringBuilder countSqlBuilder = new StringBuilder(" SELECT COUNT(*) FROM district d ");

            List<Object> params = new ArrayList<>();
            List<Object> countParams = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                sqlBuilder.append("""
                        , GREATEST(s.name_sim, s.type_sim, s.postal_sim) AS max_similarity
                        FROM district d
                        LEFT JOIN province p ON d.province_id = p.id
                        CROSS JOIN LATERAL (
                            SELECT
                                similarity(?, d.name) as name_sim,
                                similarity(?, d.type) as type_sim,
                                similarity(?, d.postal_code) as postal_sim
                        ) s
                        WHERE s.name_sim >= ? OR s.type_sim >= ? OR s.postal_sim >= ?
                        ORDER BY max_similarity DESC
                        """);

                countSqlBuilder.append("""
                        CROSS JOIN LATERAL (
                            SELECT
                                similarity(?, d.name) as name_sim,
                                similarity(?, d.type) as type_sim,
                                similarity(?, d.postal_code) as postal_sim
                        ) s
                        WHERE s.name_sim >= ? OR s.type_sim >= ? OR s.postal_sim >= ?
                        """);

                double threshold = 0.2;
                IntStream.range(0, 3).forEach(i -> {
                    params.add(search);
                    countParams.add(search);
                });
                IntStream.range(0, 3).forEach(i -> {
                    params.add(threshold);
                    countParams.add(threshold);
                });

            } else {
                sqlBuilder.append("""
                        FROM district d
                        LEFT JOIN province p ON d.province_id = p.id
                        ORDER BY d.name ASC
                        """);

                countSqlBuilder.append(" WHERE 1=1");
            }

            final String sql = sqlBuilder.toString();
            final String countSql = countSqlBuilder.toString();

            if (page != null && size != null) {
                String paginatedSql = sql + " LIMIT ? OFFSET ?";
                params.add(size);
                params.add(page * size);

                List<DistrictDTO> content = jdbcTemplate.query(paginatedSql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(content, pageable, total != null ? total : 0);
            } else {
                List<DistrictDTO> content = jdbcTemplate.query(sql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                return new PageImpl<>(content, Pageable.unpaged(), total != null ? total : 0);
            }

        } catch (Exception e) {
            throw new ErrorException("msg.internal.error", "Error fetching districts list", e);
        }
    }

    private DistrictDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        final Long id = rs.getLong("id");
        final Long provinceId = rs.getLong("province_id");
        final String name = rs.getString("name");
        final String type = rs.getString("type");
        final String postalCode = rs.getString("postal_code");
        List<CommuneDTO> communes = null;
        try {
           communes = fromJsonAsList(rs.getString("communes"), CommuneDTO[].class);
        } catch (SQLException ignored) {};
        return DistrictDTO.builder()
                .id(id)
                .provinceId(provinceId)
                .type(type)
                .name(name)
                .postalCode(postalCode)
                .communes(communes)
                .build();
    }

    public static <T> List<T> fromJsonAsList(String json, Class<T[]> className) {
        return json != null ? Arrays.asList(new Gson().fromJson(json, className)) : new ArrayList<>();
    }
}
