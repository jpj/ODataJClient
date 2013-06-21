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
package com.msopentech.odatajclient.engine.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * OData entity.
 */
public abstract class ODataEntity extends ODataItem {

    private static final long serialVersionUID = 8360640095932811034L;

    /**
     * Media entity flag.
     */
    private boolean mediaEntity = false;

    private String mediaContentType;

    private String mediaContentSource;

    /**
     * Edit link.
     */
    protected URI editLink;

    /**
     * Navigation links (might contain in-line entities or feeds).
     */
    protected final List<ODataLink> navigationLinks = new ArrayList<ODataLink>();

    /**
     * Association links.
     */
    protected final List<ODataLink> associationLinks = new ArrayList<ODataLink>();

    /**
     * Media edit links.
     */
    protected final List<ODataLink> editMediaLinks = new ArrayList<ODataLink>();

    /**
     * Entity properties.
     */
    protected final List<ODataProperty> properties = new ArrayList<ODataProperty>();

    /**
     * Constructor.
     *
     * @param name OData entity name.
     */
    public ODataEntity(final String name) {
        super(name);
    }

    /**
     * Returns OData entity properties.
     *
     * @return OData entity properties.
     */
    public List<ODataProperty> getProperties() {
        return properties;
    }

    /**
     * Adds new property.
     *
     * @param property property to be added.
     */
    public boolean addProperty(final ODataProperty property) {
        return properties.contains(property) ? false : properties.add(property);
    }

    /**
     * Removes given property.
     *
     * @param property property to be removed.
     */
    public boolean removeProperty(final ODataProperty property) {
        return properties.remove(property);
    }

    /**
     * Puts the given link into one of available lists, based on its type.
     *
     * @param link to be added
     * @return <tt>true</tt> if the given link was added in one of available lists
     */
    public boolean addLink(final ODataLink link) {
        boolean result = false;

        switch (link.getType()) {
            case ASSOCIATION:
                result = associationLinks.contains(link) ? false : associationLinks.add(link);
                break;

            case ENTITY_NAVIGATION:
            case FEED_NAVIGATION:
                result = navigationLinks.contains(link) ? false : navigationLinks.add(link);
                break;

            case MEDIA_EDIT:
                result = editMediaLinks.contains(link) ? false : editMediaLinks.add(link);
                break;

            default:
        }

        return result;
    }

    /**
     * Removes the given link from any list (association, navigation, edit-media).
     *
     * @param link to be removed
     * @return <tt>true</tt> if the given link was contained in one of available lists
     */
    public boolean removeLink(final ODataLink link) {
        return associationLinks.remove(link) || navigationLinks.remove(link) || editMediaLinks.remove(link);
    }

    /**
     * Returns all entity navigation links (including inline entities / feeds).
     *
     * @return OData entity links.
     */
    public List<ODataLink> getNavigationLinks() {
        return navigationLinks;
    }

    /**
     * Returns all entity association links.
     *
     * @return OData entity links.
     */
    public List<ODataLink> getAssociationLinks() {
        return associationLinks;
    }

    /**
     * Returns all entity media edit links.
     *
     * @return OData entity links.
     */
    public List<ODataLink> getEditMediaLinks() {
        return editMediaLinks;
    }

    /**
     * Returns OData entity edit link.
     *
     * @return entity edit link.
     */
    public URI getEditLink() {
        return editLink;
    }

    /**
     * Sets OData entity edit link.
     *
     * @param editLink edit link.
     */
    public void setEditLink(final URI editLink) {
        this.editLink = editLink;
    }

    /**
     * {@inheritDoc }
     * <p>
     * If null the edit link will be returned.
     */
    @Override
    public URI getLink() {
        return super.getLink() == null ? getEditLink() : super.getLink();
    }

    /**
     * TRUE if read-only entity.
     *
     * @return TRUE if read-only; FALSE otherwise.
     */
    public boolean isReadOnly() {
        return super.getLink() != null;
    }

    public boolean isMediaEntity() {
        return mediaEntity;
    }

    public void setMediaEntity(boolean isMediaEntity) {
        this.mediaEntity = isMediaEntity;
    }

    public String getMediaContentType() {
        return mediaContentType;
    }

    public void setMediaContentType(String mediaContentType) {
        this.mediaContentType = mediaContentType;
    }

    public String getMediaContentSource() {
        return mediaContentSource;
    }

    public void setMediaContentSource(String mediaContentSource) {
        this.mediaContentSource = mediaContentSource;
    }
}
