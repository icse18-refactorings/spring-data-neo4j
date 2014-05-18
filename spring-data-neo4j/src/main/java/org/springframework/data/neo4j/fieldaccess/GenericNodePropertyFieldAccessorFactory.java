/**
 * Copyright 2011 the original author or authors.
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

package org.springframework.data.neo4j.fieldaccess;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.neo4j.mapping.MappingPolicy;
import org.springframework.data.neo4j.mapping.Neo4jPersistentProperty;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.conversion.GenericObjectToObjectConverter;

/**
 * @author Nicki Watt
 * @since 18.05.2014
 */
@Configurable
public class GenericNodePropertyFieldAccessorFactory implements FieldAccessorFactory {

    private final Neo4jTemplate template;
    private final GenericConversionService genericConversionService;

    public GenericNodePropertyFieldAccessorFactory(Neo4jTemplate template) {
        this.template = template;
        this.genericConversionService = new GenericConversionService();
        genericConversionService.addConverter(new GenericObjectToObjectConverter());
    }

	@Override
    public boolean accept(final Neo4jPersistentProperty property) {
        return property.isSerializablePropertyField(genericConversionService);
    }

    @Override
    public FieldAccessor forField(final Neo4jPersistentProperty property) {
        return new GenericNodePropertyFieldAccessor(property, template, genericConversionService);
    }

    public static class GenericNodePropertyFieldAccessor extends PropertyFieldAccessorFactory.PropertyFieldAccessor {

        private final PropertyConverter propertyConverter;

        public GenericNodePropertyFieldAccessor(Neo4jPersistentProperty property,
                                                Neo4jTemplate template,
                                                ConversionService conversionService) {
            super(template, property);
            this.propertyConverter = new PropertyConverter(conversionService,property);
        }

        @Override
        public Object setValue(final Object entity, final Object newVal, MappingPolicy mappingPolicy) {
            Object value = propertyConverter.isObjectOrSupportedType(newVal, this.property) ? newVal : propertyConverter.serializePropertyValue(newVal);
            super.setValue(entity, value, mappingPolicy);
            return newVal;
        }

        @Override
        public Object doGetValue(final Object entity) {
            Object ret = super.doGetValue(entity);
            if (propertyConverter.isObjectOrSupportedType(ret, this.property)) {
                return ret;
            }
            return propertyConverter.deserializePropertyValue(ret);
        }

        @Override
        protected Object convertSimplePropertyValue(Object value) {
            return value;
        }
    }
}
