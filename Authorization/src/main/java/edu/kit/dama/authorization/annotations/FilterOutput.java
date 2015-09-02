/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.authorization.annotations;

import edu.kit.dama.authorization.entities.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author mf6319
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FilterOutput {
    /**
     * The role required by the user of the annotation
     * to execute the body of the annotated method
     * @return 
     */
    Role roleRequired() default edu.kit.dama.authorization.entities.Role.NO_ACCESS;


    /* NOOOOOO! Why did this stop working?!
     * Using the fully qualified class name for the annotation default value
     * as a workaround ... http://bugs.sun.com/view_bug.do?bug_id=6512707
     */

   //Class<? extends FilterLogic> strategy() default edu.dama.kit.authorization.aspects.logic.PlainFilterLogic.class;
   //    Class<PlainAccessLogic> strategy() default PlainAccessLogic.class;
}
