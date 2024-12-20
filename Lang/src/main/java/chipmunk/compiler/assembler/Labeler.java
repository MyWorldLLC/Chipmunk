/*
 * Copyright (C) 2024 MyWorld, LLC
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

package chipmunk.compiler.assembler;

import java.util.ArrayList;
import java.util.List;

public class Labeler {

    private int labelNumber;
    private List<Label> labels = new ArrayList<>();
    private List<LabelTarget> labelTargets = new ArrayList<>();

    public Label label(String labelName, int ip){
        Label label = new Label(labelName, ip);
        labels.add(label);
        return label;
    }

    public Label label(int ip){

        Label label = new Label(nextLabelName(), ip);
        labels.add(label);

        return label;
    }

    public String nextLabelName(){
        String name = Integer.toString(labelNumber);
        labelNumber++;
        return name;
    }

    public LabelTarget setLabelTarget(String label, int ip){
        LabelTarget target = new LabelTarget(label, ip);
        labelTargets.add(target);
        return target;
    }

    public LabelTarget setLabelTarget(Label label, int ip){
        LabelTarget target = new LabelTarget(label.getName(), ip);
        labelTargets.add(target);
        return target;
    }

    public Label get(int i){
        return labels.get(i);
    }

    public LabelTarget getTarget(int i){
        return labelTargets.get(i);
    }

    public int labelCount(){
        return labels.size();
    }

    public int labelTargetCount(){
        return labelTargets.size();
    }

    public List<LabelTarget> getTargets(){
        return labelTargets;
    }
}
