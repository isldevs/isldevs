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
package com.base.entity.location.commune.service;


import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.entity.location.commune.controller.CommuneConstants;
import com.base.entity.location.commune.dto.CommuneDTO;
import com.base.entity.location.commune.model.Commune;
import com.base.entity.location.commune.repository.CommuneRepository;
import com.base.entity.location.commune.validation.CommuneDataValidation;
import com.base.entity.location.district.model.District;
import com.base.entity.location.district.repository.DistrictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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
                .build().claims();
    }

    @Override
    public Map<String, Object> updateCommune(Long id, JsonCommand command) {
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
    public Map<String, Object> deleteCommune(Long id) {
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
    public CommuneDTO getCommuneById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM commune WHERE id = ?",
                    this::mapRow, id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("msg.not.found", id);
        }
    }

    @Override
    public Page<CommuneDTO> listCommunes(Integer page, Integer size, String search) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("""
                    SELECT c.id, c.district_id, c.type, c.name, c.postal_code,
                        d.name as district_name, d.type as district_type, d.postal_code as district_postal_code
                    """);

            StringBuilder countSqlBuilder = new StringBuilder(" SELECT COUNT(*) FROM commune c ");

            List<Object> params = new ArrayList<>();
            List<Object> countParams = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                sqlBuilder.append("""
                        , GREATEST(s.name_sim, s.type_sim, s.postal_sim) AS max_similarity
                        FROM commune c
                        LEFT JOIN district d ON c.district_id = d.id
                        CROSS JOIN LATERAL (
                            SELECT
                                similarity(?, c.name) as name_sim,
                                similarity(?, c.type) as type_sim,
                                similarity(?, c.postal_code) as postal_sim
                        ) s
                        WHERE s.name_sim >= ? OR s.type_sim >= ? OR s.postal_sim >= ?
                        ORDER BY max_similarity DESC
                        """);

                countSqlBuilder.append("""
                        CROSS JOIN LATERAL (
                            SELECT
                                similarity(?, c.name) as name_sim,
                                similarity(?, c.type) as type_sim,
                                similarity(?, c.postal_code) as postal_sim
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
                        FROM commune c
                        LEFT JOIN district d ON c.district_id = d.id
                        ORDER BY c.name ASC
                        """);

                countSqlBuilder.append(" WHERE 1=1");
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
                return new PageImpl<>(content, pageable, total != null ? total : 0);
            } else {
                List<CommuneDTO> content = jdbcTemplate.query(sql, this::mapRow, params.toArray());
                Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

                return new PageImpl<>(content, Pageable.unpaged(), total != null ? total : 0);
            }

        } catch (Exception e) {
            throw new ErrorException("msg.internal.error", "Error fetching districts list", e);
        }
    }

    private CommuneDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CommuneDTO.builder()
                .id(rs.getLong("id"))
                .districtId(rs.getLong("district_id"))
                .name(rs.getString("name"))
                .type(rs.getString("type"))
                .postalCode(rs.getString("postal_code"))
                .build();
    }
}
