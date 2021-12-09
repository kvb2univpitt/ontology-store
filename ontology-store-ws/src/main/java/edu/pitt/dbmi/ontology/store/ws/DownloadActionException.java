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
 * Dec 8, 2021 10:57:10 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class DownloadActionException extends Exception {

    private static final long serialVersionUID = 5556455541885928885L;

    public DownloadActionException() {
    }

    public DownloadActionException(String string) {
        super(string);
    }

    public DownloadActionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public DownloadActionException(Throwable thrwbl) {
        super(thrwbl);
    }

    public DownloadActionException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}