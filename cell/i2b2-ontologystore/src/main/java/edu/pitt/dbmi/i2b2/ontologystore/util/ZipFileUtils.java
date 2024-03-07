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
package edu.pitt.dbmi.i2b2.ontologystore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * Jan 23, 2024 1:21:42 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class ZipFileUtils {

    private ZipFileUtils() {
    }

    public static PackageFile getPackageFile(ZipEntry zipEntry, ZipFile zipFile) {
        try (InputStream is = zipFile.getInputStream(zipEntry)) {
            return (new ObjectMapper()).readValue(is, PackageFile.class);
        } catch (IOException exception) {
            return new PackageFile();
        }
    }

    public static Map<String, ZipEntry> getZipFileEntries(ZipFile zipFile) {
        Map<String, ZipEntry> zipFileEntries = new HashMap<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getSize() > 0) {
                String entryName = entry.getName();
                if (entryName.endsWith("package.json")) {
                    zipFileEntries.put("package.json", entry);
                } else {
                    zipFileEntries.put(entry.getName(), entry);
                }
            }
        }

        return zipFileEntries;
    }

}
