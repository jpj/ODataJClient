/*
 * Copyright 2013 MS OpenTech.
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
package com.msopentech.odatajclient.proxy.api.context;

public class Context {

    private final EntityContext entities;

    private final LinkContext links;

    public Context() {
        this.entities = new EntityContext();
        this.links = new LinkContext();

    }

    public EntityContext entityContext() {
        return entities;
    }

    public LinkContext linkContext() {
        return links;
    }

    public void detachAll() {
        entities.detachAll();
        links.detachAll();
    }
}