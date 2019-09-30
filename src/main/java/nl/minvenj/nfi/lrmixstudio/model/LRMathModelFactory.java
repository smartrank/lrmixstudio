/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import java.util.Collections;
import java.util.HashMap;

import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

public class LRMathModelFactory {

    private static HashMap<String, Class> models = new HashMap<>();

    private static void init() {
        if (models.isEmpty()) {
            models.put("SplitDrop Executor Edition", SplitDropThreadPool.class);
        }
    }

    public static Iterable<String> getAllModelNames() {
        init();
        return Collections.unmodifiableCollection(models.keySet());
    }

    public static String getDefaultModelName() {
        init();
        return "SplitDrop Executor Edition";
    }

    private LRMathModelFactory() {
    }

    public static LRMathModel getMathematicalModel(String id) throws InstantiationException, IllegalAccessException {

        if (id == null) {
            throw new IllegalArgumentException("Mathematical Model ID is null!");
        }

        for (String modelName : models.keySet()) {
            if (id.equalsIgnoreCase(modelName)) {
                return (LRMathModel) models.get(id).newInstance();
            }
        }

        throw new IllegalArgumentException("No math model '" + id + "' found!");
    }
}
