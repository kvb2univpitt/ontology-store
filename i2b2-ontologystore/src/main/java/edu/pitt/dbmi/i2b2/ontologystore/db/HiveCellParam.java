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
package edu.pitt.dbmi.i2b2.ontologystore.db;

import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ParamType;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * Oct 14, 2022 1:17:09 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class HiveCellParam implements RowMapper<ParamType> {

    @Override
    public ParamType mapRow(ResultSet rs, int rowNum) throws SQLException {

        ParamType param = new ParamType();
        param.setId(rs.getInt("id"));
        param.setName(rs.getString("param_name_cd"));
        param.setValue(rs.getString("value"));
        param.setDatatype(rs.getString("datatype_cd"));
        return param;
    }

}
