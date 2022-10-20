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
package edu.pitt.dbmi.i2b2.ontologystore.model;

/**
 *
 * Oct 19, 2022 8:36:56 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyProductAction {

    private String title;

    private String key;

    private boolean includeNetworkPackage;

    private boolean download;

    private boolean install;

    public OntologyProductAction() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OntologyProductAction{title=").append(title);
        sb.append(", key=").append(key);
        sb.append(", includeNetworkPackage=").append(includeNetworkPackage);
        sb.append(", download=").append(download);
        sb.append(", install=").append(install);
        sb.append('}');

        return sb.toString();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isIncludeNetworkPackage() {
        return includeNetworkPackage;
    }

    public void setIncludeNetworkPackage(boolean includeNetworkPackage) {
        this.includeNetworkPackage = includeNetworkPackage;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public boolean isInstall() {
        return install;
    }

    public void setInstall(boolean install) {
        this.install = install;
    }

}
