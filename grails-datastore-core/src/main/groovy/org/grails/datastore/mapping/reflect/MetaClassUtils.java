/*
 * Copyright 2015 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.datastore.mapping.reflect;

import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;

/**
 * Utility methods for manipulating meta classes
 *
 * @author Graeme Rocher
 * @since 5.0
 */
public class MetaClassUtils {

    public static ExpandoMetaClass getExpandoMetaClass(Class<?> aClass) {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();

        MetaClass mc = registry.getMetaClass(aClass);
        if (mc instanceof ExpandoMetaClass) {
            ExpandoMetaClass emc = (ExpandoMetaClass) mc;
            registry.setMetaClass(aClass, emc); // make permanent
            return emc;
        }

        registry.removeMetaClass(aClass);
        mc = registry.getMetaClass(aClass);
        if (mc instanceof ExpandoMetaClass) {
            return (ExpandoMetaClass) mc;
        }

        ExpandoMetaClass emc = new ExpandoMetaClass(aClass, true, true);
        emc.initialize();
        registry.setMetaClass(aClass, emc);
        return emc;
    }

}
