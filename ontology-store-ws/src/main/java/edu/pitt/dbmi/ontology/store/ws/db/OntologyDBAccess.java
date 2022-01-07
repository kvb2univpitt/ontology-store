/*
 * Copyright (C) 2022 University of Pittsburgh.
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
package edu.pitt.dbmi.ontology.store.ws.db;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 *
 * Jan 6, 2022 12:56:16 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public interface OntologyDBAccess {

    public void insertIntoSchemesTable(Path file) throws SQLException, IOException;

    public void insertIntoTableAccessTable(Path file) throws SQLException, IOException;

    public void insertIntoOntologyTable(Path file, String tableName) throws SQLException, IOException;

    public void createOntologyTable(String tableName) throws SQLException, IOException;

}
