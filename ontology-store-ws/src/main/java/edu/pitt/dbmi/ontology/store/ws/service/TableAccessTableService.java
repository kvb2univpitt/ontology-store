/*
 * Copyright (C) 2021 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ontology.store.ws.service;

import java.nio.file.Path;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 13, 2021 1:49:02 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class TableAccessTableService {

    private static final String TABLE_NAME = "table_access";

    private final JdbcTemplate ontologyDemoJdbcTemplate;
    private final TableInsertService tableInsertService;

    @Autowired
    public TableAccessTableService(JdbcTemplate ontologyDemoJdbcTemplate, TableInsertService tableInsertService) {
        this.ontologyDemoJdbcTemplate = ontologyDemoJdbcTemplate;
        this.tableInsertService = tableInsertService;
    }

    public void insert(Path file) throws SQLException {
        tableInsertService.insert(file, TABLE_NAME, ontologyDemoJdbcTemplate);
    }

}
