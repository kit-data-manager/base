/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.test;

import edu.kit.dama.mdm.base.DigitalObject;
import java.util.Date;
import uk.co.jemos.podam.api.AbstractRandomDataProviderStrategy;
import uk.co.jemos.podam.api.AttributeMetadata;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 *
 * @author jejkal
 */
public class RandomDataTest {

    public static void main(String[] args) {
        AbstractRandomDataProviderStrategy stra = new AbstractRandomDataProviderStrategy() {
            @Override
            public Long getLong(AttributeMetadata attributeMetadata) {
                if (attributeMetadata.getAttributeName().toLowerCase().contains("id")) {
                    return 0l;
                }
                return super.getLong(attributeMetadata);
            }

            @Override
            public Object getMemoizedObject(AttributeMetadata attributeMetadata) {
                if (attributeMetadata != null && attributeMetadata.getAttributeName() != null) {
                    switch (attributeMetadata.getAttributeName()) {
                        case "validFrom":
                            return new Date(0);
                        case "startDate":
                            return new Date(0);
                        case "validUntil":
                            return new Date(System.currentTimeMillis());
                        case "endDate":
                            return new Date(System.currentTimeMillis());
                    }
                }
                return super.getMemoizedObject(attributeMetadata);
            }

        };
        stra.setDefaultNumberOfCollectionElements(1);

        PodamFactory factory = new PodamFactoryImpl(stra);
        DigitalObject a = factory.manufacturePojo(DigitalObject.class);

        
        
        DigitalObject a2 = factory.manufacturePojoWithFullData(DigitalObject.class);

        System.out.println(a);
        System.out.println(a2);

    }

}
