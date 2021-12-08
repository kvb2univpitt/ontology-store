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
package edu.pitt.dbmi.ontology.store.ws;

/**
 *
 * Dec 8, 2021 11:22:16 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class InstallActionException extends Exception {

    private static final long serialVersionUID = 4576156600948948600L;

    public InstallActionException() {
    }

    public InstallActionException(String string) {
        super(string);
    }

    public InstallActionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public InstallActionException(Throwable thrwbl) {
        super(thrwbl);
    }

    public InstallActionException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
