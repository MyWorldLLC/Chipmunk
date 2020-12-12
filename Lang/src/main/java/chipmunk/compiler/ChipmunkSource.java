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

package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ChipmunkSource {
    
    protected final InputStream is;
    protected final String fileName;
    
    public ChipmunkSource(InputStream in, String fileName){
        is = in;
        this.fileName = fileName;
    }

    public InputStream getIs() {
        return is;
    }

    public String getFileName() {
        return fileName;
    }

    public CharSequence readFully() throws CompileChipmunk {
        StringBuilder builder = new StringBuilder();

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            int character = reader.read();
            while(character != -1){
                builder.append((char) character);
                character = reader.read();
            }
        }catch(IOException ex){
            throw new CompileChipmunk("Failed to load source", ex);
        }

        return builder;
    }

    @Override
    public String toString() {
        return "[ChipmunkSource: " + fileName + "]";
    }
}
