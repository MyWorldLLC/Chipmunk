/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.pkg;

import java.util.Properties;

public class PackageProperties {

    public static final String PACKAGE_FILE = "package.properties";

    public static final String ENTRYPOINT_KEY = "entrypoint";
    public static final String AUTHOR_KEY = "author";
    public static final String COPYRIGHT_KEY = "copyright";
    public static final String LICENSE_KEY = "license";
    public static final String VERSION_KEY = "license";

    protected Properties properties;

    public PackageProperties(){
        this(new Properties());
    }

    public PackageProperties(Properties properties){
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Entrypoint getEntrypoint(){
        String entrypoint = properties.getProperty(ENTRYPOINT_KEY);
        if(entrypoint == null){
            return null;
        }

        return Entrypoint.fromString(entrypoint);
    }

    public void setEntrypoint(Entrypoint entrypoint){
        properties.setProperty(ENTRYPOINT_KEY, entrypoint.toString());
    }

    public String getAuthor(){
        return properties.getProperty(AUTHOR_KEY);
    }

    public void setAuthor(String author){
        properties.setProperty(AUTHOR_KEY, author);
    }

    public String getCopyright(){
        return properties.getProperty(COPYRIGHT_KEY);
    }

    public void setCopyright(String copyright){
        properties.setProperty(COPYRIGHT_KEY, copyright);
    }

    public String getLicense(){
        return properties.getProperty(LICENSE_KEY);
    }

    public void setLicense(String license){
        properties.setProperty(LICENSE_KEY, license);
    }
}
