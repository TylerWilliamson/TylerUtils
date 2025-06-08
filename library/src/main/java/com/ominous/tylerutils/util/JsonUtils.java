/*
 * Copyright 2020 - 2025 Tyler Williamson
 *
 * This file is part of TylerUtils.
 *
 * TylerUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TylerUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.util;

import com.ominous.tylerutils.annotation.JSONFieldName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class JsonUtils {
    public static <T> T[] deserialize(Class<T> objClass, JSONArray json)
            throws IllegalAccessException, InstantiationException, JSONException {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(objClass, json.length());

        for (int i = 0, l = json.length(); i < l; i++) {
            array[i] = deserialize(objClass, json.getJSONObject(i));
        }

        return array;
    }

    //TODO Include other wrapped primitives
    public static <T> T deserialize(Class<T> objClass, JSONObject json)
            throws IllegalAccessException, InstantiationException, JSONException {

        T obj = objClass.newInstance();

        for (Field field : objClass.getFields()) {
            JSONFieldName fieldAnnotation = field.getAnnotation(JSONFieldName.class);
            String fieldName = fieldAnnotation == null ? field.getName() : fieldAnnotation.name();

            if (json.has(fieldName) && !json.isNull(fieldName)) {
                if (field.getType().isPrimitive() ||
                        field.getType().equals(String.class) ||
                        field.getType().equals(Boolean.class)) {
                    field.set(obj, json.get(fieldName));
                } else if (field.getType().isArray()) {
                    field.set(obj, field.getType().getComponentType() != null &&
                            field.getType().getComponentType().isPrimitive() ?
                            deserializePrimitiveArray(field.getType().getComponentType(), json.getJSONArray(fieldName)) :
                            deserialize(field.getType().getComponentType(), json.getJSONArray(fieldName)));
                } else {
                    field.set(obj, deserialize(field.getType(), json.getJSONObject(fieldName)));
                }
            }
        }

        return obj;
    }

    public static Object deserializePrimitiveArray(Class<?> objClass, JSONArray json)
            throws JSONException {
        Object array = Array.newInstance(objClass, json.length());

        for (int i = 0, l = json.length(); i < l; i++) {
            Array.set(array, i, json.get(i));
        }

        return array;
    }
}
