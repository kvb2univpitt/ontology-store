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
package edu.pitt.dbmi.i2b2.ontologystore.ejb;

/**
 *
 * Oct 12, 2022 5:20:11 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class DBInfo {

    private String hive;
    private String projectId;
    private String ownerId;
    private String dbFullSchema;
    private String dbDataSource;
    private String dbServerType;

    public DBInfo() {
    }

    public String getHive() {
        return hive;
    }

    public void setHive(String hive) {
        this.hive = hive;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getDbFullSchema() {
        return dbFullSchema;
    }

    public void setDbFullSchema(String dbFullSchema) {
        this.dbFullSchema = dbFullSchema.endsWith(".")
                ? dbFullSchema
                : dbFullSchema + ".";
    }

    public String getDbDataSource() {
        return dbDataSource;
    }

    public void setDbDataSource(String dbDataSource) {
        this.dbDataSource = dbDataSource;
    }

    public String getDbServerType() {
        return dbServerType;
    }

    public void setDbServerType(String dbServerType) {
        this.dbServerType = dbServerType;
    }

}
