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
package com.msopentech.odatajclient.engine.client.response;

import com.msopentech.odatajclient.engine.communication.response.ODataBatchResponse;
import com.msopentech.odatajclient.engine.communication.response.ODataBatchResponseItem;
import com.msopentech.odatajclient.engine.communication.response.ODataChangeset;
import com.msopentech.odatajclient.engine.communication.response.ODataQueryResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements a response to a batch request.
 *
 * @see com.msopentech.odatajclient.engine.communication.request.ODataBatchRequest
 */
abstract class ODataBatchResponseImpl extends ODataResponseImpl implements ODataBatchResponse {

    /**
     * Batch request content.
     */
    private final List<ODataBatchResponseItem> batch = new ArrayList<ODataBatchResponseItem>();

    /**
     * Adds a query response to the batch response.
     * <p>
     * Each query response is about a query request submitted embedded into a batch request.
     *
     * @param response query response to be added.
     * @return the current batch response.
     *
     * @see ODataQueryResponse.
     */
    protected ODataBatchResponseImpl addItem(final ODataQueryResponse response) {
        batch.add(new ODataRetrieveImpl(response));
        return this;
    }

    /**
     * Add a changeset to the batch response.
     *
     * @param item changeset to be added.
     * @return the current batch response.
     */
    protected ODataBatchResponseImpl addResponse(final ODataChangeset item) {
        batch.add(item);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ODataBatchResponseItem> getBody() {
        return batch.iterator();
    }
}
