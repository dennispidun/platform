/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A test serializer for {@link Product}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProductJsonSerializer implements Serializer<Product> {

    @SuppressWarnings("unchecked")
    @Override
    public byte[] to(Product value) throws IOException {
        JSONObject json = new JSONObject();
        json.put("description", value.getDescription());
        json.put("price", value.getPrice());
        return json.toJSONString().getBytes();
    }

    @Override
    public Product from(byte[] data) throws IOException {
        Product result;
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(new String(data));
            result = new Product(readString(obj, "description"), readDouble(obj, "price", 0));
        } catch (ParseException e) {
            throw new IOException(e.getMessage(), e);
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public Product clone(Product origin) throws IOException {
        return new Product(origin.getDescription(), origin.getPrice());
    }

    /**
     * Reads a string field from a JSON object.
     * 
     * @param obj   the object to read from
     * @param field the field to read from
     * @return the string value or <b>null</b>
     */
    private static String readString(JSONObject obj, String field) {
        Object tmp = obj.get(field);
        return null == tmp ? null : tmp.toString();
    }

    /**
     * Reads a double field from a JSON object.
     * 
     * @param obj   the object to read from
     * @param field the field to read from
     * @param dflt  the default value
     * @return the double value, if not accessible {@code dflt}
     * @throws IOException if parsing the double value fails
     */
    private static double readDouble(JSONObject obj, String field, double dflt) throws IOException {
        try {
            Object tmp = obj.get(field);
            return null == tmp ? dflt : Double.parseDouble(tmp.toString());
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public Class<Product> getType() {
        return Product.class;
    }

}
