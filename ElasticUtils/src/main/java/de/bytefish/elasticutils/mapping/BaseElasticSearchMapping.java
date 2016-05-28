// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.mapping;

import com.google.common.collect.ImmutableMap;
import de.bytefish.elasticutils.exceptions.GetMappingFailedException;
import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;

import java.io.IOException;

public abstract class BaseElasticSearchMapping implements IElasticSearchMapping {

    private final String indexType;
    private final String version;

    public BaseElasticSearchMapping(String indexType, String version) {
        this.indexType = indexType;
        this.version = version;
    }

    public XContentBuilder getMapping() {
        try {
            return internalGetMapping();
        } catch(Exception e) {
            throw new GetMappingFailedException(indexType, e);
        }
    }

    public String getIndexType() {
        return indexType;
    }

    public String getVersion() { return version; }

    public XContentBuilder internalGetMapping() throws IOException {

        // Configure the RootObjectMapper:
        RootObjectMapper.Builder rootObjectMapperBuilder = getRootObjectBuilder();

        // Populate the Settings:
        Settings.Builder settingsBuilder = getSettingsBuilder();

        // Build the Mapping:
        Mapping mapping = new Mapping(
                Version.fromString(version),
                rootObjectMapperBuilder.build(new Mapper.BuilderContext(settingsBuilder.build(), new ContentPath())),
                getMetaDataFieldMappers(),
                getSourceTransforms(),
                getMetaData());

        // Turn it into JsonXContent:
        return mapping.toXContent(JsonXContent.contentBuilder().startObject(), ToXContent.EMPTY_PARAMS);
    }

    private Settings.Builder getSettingsBuilder() {
        Settings.Builder settingsBuilder = Settings.builder();

        configure(settingsBuilder);

        return settingsBuilder;
    }

    private RootObjectMapper.Builder getRootObjectBuilder() {
        RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder(indexType);

        configure(rootObjectMapperBuilder);

        return rootObjectMapperBuilder;
    }

    protected abstract void configure(RootObjectMapper.Builder builder);

    protected abstract void configure(Settings.Builder builder);

    protected MetadataFieldMapper[] getMetaDataFieldMappers() {
        return new MetadataFieldMapper[]{};
    }

    protected Mapping.SourceTransform[] getSourceTransforms() {
        return new Mapping.SourceTransform[]{};
    }
    
    protected ImmutableMap<String, Object> getMetaData() {
        return null;
    }
}
