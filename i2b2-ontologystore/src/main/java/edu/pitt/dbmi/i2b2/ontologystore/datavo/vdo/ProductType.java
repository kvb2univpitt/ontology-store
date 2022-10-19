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
package edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo;

/**
 *
 * Oct 18, 2022 6:15:15 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class ProductType {

    private String fileName;

    private String title;

    private String version;

    private String owner;

    private String type;

    private boolean includeNetworkPackage;

    private String[] terminologies;

    private boolean downloaded;

    private boolean installed;

    private boolean started;

    private boolean failed;

    public ProductType() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Product{");
        sb.append("fileName=").append(fileName);
        sb.append(", title=").append(title);
        sb.append(", version=").append(version);
        sb.append(", owner=").append(owner);
        sb.append(", type=").append(type);
        sb.append(", includeNetworkPackage=").append(includeNetworkPackage);
        sb.append(", terminologies=").append(terminologies);
        sb.append(", downloaded=").append(downloaded);
        sb.append(", installed=").append(installed);
        sb.append(", started=").append(started);
        sb.append(", failed=").append(failed);
        sb.append('}');
        return sb.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIncludeNetworkPackage() {
        return includeNetworkPackage;
    }

    public void setIncludeNetworkPackage(boolean includeNetworkPackage) {
        this.includeNetworkPackage = includeNetworkPackage;
    }

    public String[] getTerminologies() {
        return terminologies;
    }

    public void setTerminologies(String[] terminologies) {
        this.terminologies = terminologies;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

}
