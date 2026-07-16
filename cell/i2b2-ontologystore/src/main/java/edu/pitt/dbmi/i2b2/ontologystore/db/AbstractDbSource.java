/*
 * Copyright (C) 2026 University of Pittsburgh.
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
package edu.pitt.dbmi.i2b2.ontologystore.db;

/**
 *
 * Jul 15, 2026 8:15:09 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractDbSource implements DbSource {

    private final String schema;

    private final String jndiDatasource;

    public AbstractDbSource(String schema, String jndiDatasource) {
        this.schema = schema;
        this.jndiDatasource = jndiDatasource;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getJndiDatasource() {
        return jndiDatasource;
    }

}
