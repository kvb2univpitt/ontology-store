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
package edu.pitt.dbmi.ontology.store.ws.model;

/**
 *
 * Oct 27, 2021 3:28:09 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyStoreObject {

    private String productTitle;

    private String productVersion;

    private String productOwner;

    private String productType;

    private String includeNetworkPackage;

    private String[] tableAccess;

    private String schemes;

    private String breakdownPath;

    private String adapterMapping;

    private String shrineIndex;

    private String[] conceptDimensions;

    private String[] terminologies;

    private String[] listOfDomainOntologies;

    public OntologyStoreObject() {
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getProductOwner() {
        return productOwner;
    }

    public void setProductOwner(String productOwner) {
        this.productOwner = productOwner;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getIncludeNetworkPackage() {
        return includeNetworkPackage;
    }

    public void setIncludeNetworkPackage(String includeNetworkPackage) {
        this.includeNetworkPackage = includeNetworkPackage;
    }

    public String[] getTableAccess() {
        return tableAccess;
    }

    public void setTableAccess(String[] tableAccess) {
        this.tableAccess = tableAccess;
    }

    public String getSchemes() {
        return schemes;
    }

    public void setSchemes(String schemes) {
        this.schemes = schemes;
    }

    public String getBreakdownPath() {
        return breakdownPath;
    }

    public void setBreakdownPath(String breakdownPath) {
        this.breakdownPath = breakdownPath;
    }

    public String getAdapterMapping() {
        return adapterMapping;
    }

    public void setAdapterMapping(String adapterMapping) {
        this.adapterMapping = adapterMapping;
    }

    public String getShrineIndex() {
        return shrineIndex;
    }

    public void setShrineIndex(String shrineIndex) {
        this.shrineIndex = shrineIndex;
    }

    public String[] getConceptDimensions() {
        return conceptDimensions;
    }

    public void setConceptDimensions(String[] conceptDimensions) {
        this.conceptDimensions = conceptDimensions;
    }

    public String[] getTerminologies() {
        return terminologies;
    }

    public void setTerminologies(String[] terminologies) {
        this.terminologies = terminologies;
    }

    public String[] getListOfDomainOntologies() {
        return listOfDomainOntologies;
    }

    public void setListOfDomainOntologies(String[] listOfDomainOntologies) {
        this.listOfDomainOntologies = listOfDomainOntologies;
    }

}
