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
package com.msopentech.odatajclient.proxy.api.impl;

import com.msopentech.odatajclient.proxy.api.context.AttachedEntityStatus;

public class AttachedEntity {

    private final EntityTypeInvocationHandler entity;

    private final AttachedEntityStatus status;

    public AttachedEntity(final EntityTypeInvocationHandler entity, final AttachedEntityStatus status) {
        this.entity = entity;
        this.status = status;
    }

    public EntityTypeInvocationHandler getEntity() {
        return entity;
    }

    public AttachedEntityStatus getStatus() {
        return status;
    }
}