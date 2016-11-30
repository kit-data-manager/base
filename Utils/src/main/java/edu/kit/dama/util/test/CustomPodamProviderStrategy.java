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
package edu.kit.dama.util.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import uk.co.jemos.podam.api.AttributeMetadata;
import uk.co.jemos.podam.api.DataProviderStrategy;
import uk.co.jemos.podam.api.RandomDataProviderStrategy;
import uk.co.jemos.podam.api.RandomDataProviderStrategyImpl;
import uk.co.jemos.podam.common.AbstractConstructorComparator;
import uk.co.jemos.podam.common.AbstractMethodComparator;
import uk.co.jemos.podam.common.AttributeStrategy;

/**
 *
 * @author jejkal
 */
public class CustomPodamProviderStrategy implements RandomDataProviderStrategy {

    final RandomDataProviderStrategyImpl impl = new RandomDataProviderStrategyImpl();

    @Override
    public int getNumberOfCollectionElements(Class<?> type) {
        return 1;
    }

    @Override
    public RandomDataProviderStrategy addOrReplaceAttributeStrategy(Class<? extends Annotation> type, Class<AttributeStrategy<?>> type1) {
        return impl.addOrReplaceAttributeStrategy(type, type1);
    }

    @Override
    public RandomDataProviderStrategy removeAttributeStrategy(Class<? extends Annotation> type) {
        return impl.removeAttributeStrategy(type);
    }

    @Override
    public AbstractConstructorComparator getConstructorLightComparator() {
        return impl.getConstructorLightComparator();
    }

    @Override
    public void setConstructorLightComparator(AbstractConstructorComparator acc) {
        impl.setConstructorLightComparator(acc);
    }

    @Override
    public AbstractConstructorComparator getConstructorHeavyComparator() {
        return impl.getConstructorHeavyComparator();
    }

    @Override
    public void setConstructorHeavyComparator(AbstractConstructorComparator acc) {
        impl.setConstructorHeavyComparator(acc);
    }

    @Override
    public AbstractMethodComparator getMethodLightComparator() {
        return impl.getMethodLightComparator();
    }

    @Override
    public void setMethodLightComparator(AbstractMethodComparator amc) {
        impl.setMethodLightComparator(amc);
    }

    @Override
    public AbstractMethodComparator getMethodHeavyComparator() {
        return impl.getMethodHeavyComparator();
    }

    @Override
    public void setMethodHeavyComparator(AbstractMethodComparator amc) {
        impl.setMethodHeavyComparator(amc);
    }

    @Override
    public <T> DataProviderStrategy addOrReplaceSpecific(Class<T> type, Class<? extends T> type1) {
        return impl.addOrReplaceSpecific(type, type1);
    }

    @Override
    public <T> DataProviderStrategy removeSpecific(Class<T> type) {
        return impl.removeSpecific(type);
    }

    @Override
    public Boolean getBoolean(AttributeMetadata am) {
        return impl.getBoolean(am);
    }

    @Override
    public Byte getByte(AttributeMetadata am) {
        return impl.getByte(am);
    }

    @Override
    public Byte getByteInRange(byte b, byte b1, AttributeMetadata am) {
        return impl.getByteInRange(b, b1, am);
    }

    @Override
    public Character getCharacter(AttributeMetadata am) {
        return impl.getCharacter(am);
    }

    @Override
    public Character getCharacterInRange(char c, char c1, AttributeMetadata am) {
        return impl.getCharacterInRange(c, c1, am);
    }

    @Override
    public Double getDouble(AttributeMetadata am) {
        return impl.getDouble(am);
    }

    @Override
    public Double getDoubleInRange(double d, double d1, AttributeMetadata am) {
        return impl.getDoubleInRange(d, d1, am);
    }

    @Override
    public Float getFloat(AttributeMetadata am) {
        return impl.getFloat(am);
    }

    @Override
    public Float getFloatInRange(float f, float f1, AttributeMetadata am) {
        return impl.getFloatInRange(f, f1, am);
    }

    @Override
    public Integer getInteger(AttributeMetadata am) {
        return impl.getInteger(am);
    }

    @Override
    public int getIntegerInRange(int i, int i1, AttributeMetadata am) {
        return impl.getIntegerInRange(i, i1, am);
    }

    @Override
    public Long getLong(AttributeMetadata am) {
        return impl.getLong(am);
    }

    @Override
    public Long getLongInRange(long l, long l1, AttributeMetadata am) {
        return impl.getLongInRange(l, l1, am);
    }

    @Override
    public Short getShort(AttributeMetadata am) {
        return impl.getShort(am);
    }

    @Override
    public Short getShortInRange(short s, short s1, AttributeMetadata am) {
        return impl.getShortInRange(s, s1, am);
    }

    @Override
    public String getStringValue(AttributeMetadata am) {
        if (am.getAttributeName().contains("mail")) {
            return "test@mail.org";
        }
        return impl.getStringValue(am);
    }

    @Override
    public String getStringOfLength(int i, AttributeMetadata am) {
        return impl.getStringOfLength(i, am);
    }

    @Override
    public void setDefaultNumberOfCollectionElements(int i) {
        //not supported
    }

    @Override
    public int getMaxDepth(Class<?> type) {
        return impl.getMaxDepth(type);
    }

    @Override
    public boolean isMemoizationEnabled() {
        return impl.isMemoizationEnabled();
    }

    @Override
    public void setMemoization(boolean bln) {
        impl.setMemoization(bln);
    }

    @Override
    public Object getMemoizedObject(AttributeMetadata am) {
        return impl.getMemoizedObject(am);
    }

    @Override
    public void cacheMemoizedObject(AttributeMetadata am, Object o) {
        impl.cacheMemoizedObject(am, o);
    }

    @Override
    public void clearMemoizationCache() {
        impl.clearMemoizationCache();
    }

    @Override
    public void sort(Constructor<?>[] cs, DataProviderStrategy.Order order) {
        impl.sort(cs, order);
    }

    @Override
    public void sort(Method[] methods, DataProviderStrategy.Order order) {
        impl.sort(methods, order);
    }

    @Override
    public <T> Class<? extends T> getSpecificClass(Class<T> type) {
        return impl.getSpecificClass(type);
    }

    @Override
    public Class<AttributeStrategy<?>> getStrategyForAnnotation(Class<? extends Annotation> type) {
        return impl.getStrategyForAnnotation(type);
    }
}
