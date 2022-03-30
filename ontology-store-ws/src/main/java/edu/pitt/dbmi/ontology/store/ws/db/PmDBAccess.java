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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Mar 25, 2022 1:41:51 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PmDBAccess {

    private static final String ADMIN_ROLE_SQL_TEMPLATE = "SELECT DISTINCT ppur.user_role_cd "
            + "FROM %s.pm_user_data pud "
            + "LEFT JOIN %s.pm_project_user_roles ppur ON pud.user_id = ppur.user_id AND ppur.status_cd <> 'D' AND ppur.user_role_cd = 'ADMIN' "
            + "WHERE pud.user_id = ?";
    private static final String SESSION_EXPIRATION_SQL_TEMPLATE = "SELECT expired_date FROM %s.pm_user_session WHERE user_id = ? AND session_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public PmDBAccess(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public boolean isAdmin(String userId) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                String sql = String.format(ADMIN_ROLE_SQL_TEMPLATE, conn.getSchema(), conn.getSchema());
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, userId);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String role = rs.getString("user_role_cd");
                    if (role != null && role.trim().toLowerCase().equals("admin")) {
                        return true;
                    }
                }

            }
        }

        return false;
    }

    public boolean hasExpiredSession(String username, String password) throws SQLException {
        LocalDateTime expiration = getSessionExpiration(username, password);
        if (expiration == null) {
            return true;
        }

        return LocalDateTime.now().compareTo(expiration) > 0;
    }

    private LocalDateTime getSessionExpiration(String username, String password) throws SQLException {
        LocalDateTime expiration = null;

        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                String sql = String.format(SESSION_EXPIRATION_SQL_TEMPLATE, conn.getSchema());
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    Timestamp expirationDate = resultSet.getTimestamp("expired_date");
                    if (expirationDate != null) {
                        expiration = expirationDate.toLocalDateTime();
                    }
                }

            }
        }

        return expiration;
    }

}
