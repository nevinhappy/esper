/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.event.bean;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventPropertyGetterAndMapped;
import com.espertech.esper.event.vaevent.PropertyUtility;
import com.espertech.esper.util.JavaClassHelper;

import java.lang.reflect.Field;
import java.util.Map;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.constant;

/**
 * Getter for a key property identified by a given key value, using vanilla reflection.
 */
public class KeyedMapFieldPropertyGetter extends BaseNativePropertyGetter implements BeanEventPropertyGetter, EventPropertyGetterAndMapped {
    private final Field field;
    private final Object key;

    /**
     * Constructor.
     *
     * @param field               is the field to use to retrieve a value from the object.
     * @param key                 is the key to supply as parameter to the mapped property getter
     * @param eventAdapterService factory for event beans and event types
     */
    public KeyedMapFieldPropertyGetter(Field field, Object key, EventAdapterService eventAdapterService) {
        super(eventAdapterService, JavaClassHelper.getGenericFieldTypeMap(field, false), null);
        this.key = key;
        this.field = field;
    }

    public Object get(EventBean eventBean, String mapKey) throws PropertyAccessException {
        return getBeanPropInternal(eventBean.getUnderlying(), mapKey);
    }

    public Object getBeanProp(Object object) throws PropertyAccessException {
        return getBeanPropInternal(object, key);
    }

    public Object getBeanPropInternal(Object object, Object key) throws PropertyAccessException {
        try {
            Object result = field.get(object);
            if (!(result instanceof Map)) {
                return null;
            }
            Map resultMap = (Map) result;
            return resultMap.get(key);
        } catch (ClassCastException e) {
            throw PropertyUtility.getMismatchException(field, object, e);
        } catch (IllegalAccessException e) {
            throw PropertyUtility.getIllegalAccessException(field, e);
        } catch (IllegalArgumentException e) {
            throw PropertyUtility.getIllegalArgumentException(field, e);
        }
    }

    private String getBeanPropInternalCodegen(CodegenContext context) throws PropertyAccessException {
        return context.addMethod(getBeanPropType(), this.getClass()).add(getTargetType(), "object").add(Object.class, "key").begin()
                .declareVar(Object.class, "result", exprDotName(ref("object"), field.getName()))
                .ifRefNotTypeReturnConst("result", Map.class, null)
                .declareVarWCast(Map.class, "map", "result")
                .methodReturn(cast(getBeanPropType(), exprDotMethod(ref("map"), "get", ref("key"))));
    }

    public boolean isBeanExistsProperty(Object object) {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public final Object get(EventBean obj) throws PropertyAccessException {
        Object underlying = obj.getUnderlying();
        return getBeanProp(underlying);
    }

    public String toString() {
        return "KeyedMapFieldPropertyGetter " +
                " field=" + field.toString() +
                " key=" + key;
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true; // Property exists as the property is not dynamic (unchecked)
    }

    public Class getBeanPropType() {
        return JavaClassHelper.getGenericFieldTypeMap(field, false);
    }

    public Class getTargetType() {
        return field.getDeclaringClass();
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return underlyingGetCodegen(castUnderlying(getTargetType(), beanExpression), context);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return localMethod(getBeanPropInternalCodegen(context), underlyingExpression, constant(key));
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression eventBeanGetMappedCodegen(CodegenContext context, CodegenExpression beanExpression, CodegenExpression key) {
        return localMethod(getBeanPropInternalCodegen(context), castUnderlying(getTargetType(), beanExpression), key);
    }
}