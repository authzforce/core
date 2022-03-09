/*
 * Copyright 2012-2022 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.io.xacml.json;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Serializable version of JSONObject (wrapped)
 */
public class SerializableJSONObject implements Serializable
{
    private static final long serialVersionUID = 1L; // use a real serial ID here

    private static JSONObject copyOf(JSONObject jsonObject)
    {
        assert jsonObject != null;
        return new JSONObject(jsonObject, jsonObject.keySet().toArray(new String[0]));
    }

    private transient JSONObject jsonObject;

// some other non-transient fields

    private void writeObject(ObjectOutputStream oos) throws IOException
    {
        // default serialization
        oos.defaultWriteObject();
        // write the object
        oos.writeUTF(jsonObject.toString());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
    {
        // default deserialization
        ois.defaultReadObject();
        jsonObject = new JSONObject(ois.readUTF());
    }

    /**
     * Creates Serializable object based on input JSON object
     *
     * @param jsonObject jsonObject wrapped JSON object
     */
    public SerializableJSONObject(JSONObject jsonObject)
    {
        this.jsonObject = copyOf(jsonObject);
    }

    /**
     * Gets wrapped JSON object
     *
     * @return JSON object
     */
    public JSONObject get()
    {
        // defensive copy
        return copyOf(this.jsonObject);
    }
}
