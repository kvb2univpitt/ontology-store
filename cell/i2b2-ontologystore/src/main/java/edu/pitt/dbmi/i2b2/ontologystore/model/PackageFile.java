/*
 * Copyright (C) 2024 University of Pittsburgh.
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
package edu.pitt.dbmi.i2b2.ontologystore.model;

/**
 * This class represent data from
 * <pre>package.json.</pre> file.
 *
 * Dec 6, 2023 1:57:47 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class PackageFile {

    private String[] tableAccess;

    private String[] schemes;

    private String[] breakdownPath;

    private String[] adapterMapping;

    private String[] shrineIndex;

    private String[] conceptDimensions;

    private String[] domainOntologies;

    public PackageFile() {
    }

    public String[] getTableAccess() {
        return tableAccess;
    }

    public void setTableAccess(String[] tableAccess) {
        this.tableAccess = tableAccess;
    }

    public String[] getSchemes() {
        return schemes;
    }

    public void setSchemes(String[] schemes) {
        this.schemes = schemes;
    }

    public String[] getBreakdownPath() {
        return breakdownPath;
    }

    public void setBreakdownPath(String[] breakdownPath) {
        this.breakdownPath = breakdownPath;
    }

    public String[] getAdapterMapping() {
        return adapterMapping;
    }

    public void setAdapterMapping(String[] adapterMapping) {
        this.adapterMapping = adapterMapping;
    }

    public String[] getShrineIndex() {
        return shrineIndex;
    }

    public void setShrineIndex(String[] shrineIndex) {
        this.shrineIndex = shrineIndex;
    }

    public String[] getConceptDimensions() {
        return conceptDimensions;
    }

    public void setConceptDimensions(String[] conceptDimensions) {
        this.conceptDimensions = conceptDimensions;
    }

    public String[] getDomainOntologies() {
        return domainOntologies;
    }

    public void setDomainOntologies(String[] domainOntologies) {
        this.domainOntologies = domainOntologies;
    }

}
