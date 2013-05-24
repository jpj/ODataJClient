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
package com.msopentech.odatajclient.engine.communication.request;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a batch request.
 * Get instance by using ODataRequestFactory.
 *
 * @see ODataRequestFactory#getBatchRequest().
 */
public class ODataBatchRequest extends ODataRequest {

    /**
     * Batch request content.
     */
    private final List<ODataBatchRequestItem> batch = new ArrayList<ODataBatchRequestItem>();

    /**
     * Constructor.
     */
    ODataBatchRequest() {
        super(Method.POST);
    }

    /**
     * Adds a retrieve operation request.
     *
     * @param item retrieve operation request to be added.
     * @return the current updated request.
     */
    public ODataBatchRequest addItem(final ODataQueryRequest item) {
        batch.add(ODataRequestFactory.getRetrieveBatchItem(item));
        return this;
    }

    /**
     * Add a changeset to the batch.
     *
     * @param item changeset to be added.
     * @return the current updated request.
     */
    public ODataBatchRequest addItem(final ODataChangeset item) {
        batch.add(item);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBody() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}