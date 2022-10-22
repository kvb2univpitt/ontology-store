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
package edu.pitt.dbmi.i2b2.ontologystore;

/**
 *
 * Oct 22, 2022 5:30:43 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class InstallationException extends Exception {

    private static final long serialVersionUID = -1581825775607254871L;

    public InstallationException() {
    }

    public InstallationException(String string) {
        super(string);
    }

    public InstallationException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InstallationException(Throwable thrwbl) {
        super(thrwbl);
    }

    public InstallationException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
