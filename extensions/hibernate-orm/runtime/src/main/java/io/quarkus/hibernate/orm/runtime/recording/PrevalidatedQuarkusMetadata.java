package io.quarkus.hibernate.orm.runtime.recording;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.internal.SessionFactoryOptionsBuilder;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.TypeDefinition;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.annotations.NamedEntityGraphDefinition;
import org.hibernate.cfg.annotations.NamedProcedureCallDefinition;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;

/**
 * This is a Quarkus custom implementation of Metadata wrapping the original
 * implementation from Hibernate ORM.
 * The goal is to run the {@link MetadataImpl#validate()} method
 * earlier than when it is normally performed, for two main reasons: further reduce
 * the work that is still necessary when performing a runtime boot, and to be
 * able to still use reflection as it's neccessary e.g. to validate enum fields.
 *
 * We also make sure that methods {@link #getSessionFactoryBuilder()} and {@link #buildSessionFactory()}
 * are unavailable, as these would normally trigger an additional validation phase:
 * we can actually boot Quarkus in a simpler way.
 */
public final class PrevalidatedQuarkusMetadata implements Metadata {

    private final MetadataImpl metadata;

    private PrevalidatedQuarkusMetadata(final MetadataImpl metadata) {
        this.metadata = metadata;
    }

    public static PrevalidatedQuarkusMetadata validateAndWrap(final MetadataImpl original) {
        original.validate();
        return new PrevalidatedQuarkusMetadata(original);
    }

    // New helpers on this Quarkus specific metadata; these are useful to boot and manage the recorded state:

    public SessionFactoryOptionsBuilder buildSessionFactoryOptionsBuilder() {
        return new SessionFactoryOptionsBuilder(
                metadata.getMetadataBuildingOptions().getServiceRegistry(),
                metadata.getBootstrapContext());
    }

    public MetadataImplementor getOriginalMetadata() {
        return metadata;
    }

    //Relevant overrides:

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder() {
        //Ensure we don't boot Hibernate using this, but rather use the #buildSessionFactoryOptionsBuilder above.
        throw new IllegalStateException("This method is not supposed to be used in Quarkus");
    }

    @Override
    public SessionFactory buildSessionFactory() {
        //Ensure we don't boot Hibernate using this, but rather use the #buildSessionFactoryOptionsBuilder above.
        throw new IllegalStateException("This method is not supposed to be used in Quarkus");
    }

    //All other contracts from Metadata delegating:

    @Override
    public UUID getUUID() {
        return metadata.getUUID();
    }

    @Override
    public Database getDatabase() {
        return metadata.getDatabase();
    }

    @Override
    public Collection<PersistentClass> getEntityBindings() {
        return metadata.getEntityBindings();
    }

    @Override
    public PersistentClass getEntityBinding(final String entityName) {
        return metadata.getEntityBinding(entityName);
    }

    @Override
    public Collection<org.hibernate.mapping.Collection> getCollectionBindings() {
        return metadata.getCollectionBindings();
    }

    @Override
    public org.hibernate.mapping.Collection getCollectionBinding(final String role) {
        return metadata.getCollectionBinding(role);
    }

    @Override
    public Map<String, String> getImports() {
        return metadata.getImports();
    }

    @Override
    public NamedQueryDefinition getNamedQueryDefinition(final String name) {
        return metadata.getNamedQueryDefinition(name);
    }

    @Override
    public Collection<NamedQueryDefinition> getNamedQueryDefinitions() {
        return metadata.getNamedQueryDefinitions();
    }

    @Override
    public NamedSQLQueryDefinition getNamedNativeQueryDefinition(final String name) {
        return metadata.getNamedNativeQueryDefinition(name);
    }

    @Override
    public Collection<NamedSQLQueryDefinition> getNamedNativeQueryDefinitions() {
        return metadata.getNamedNativeQueryDefinitions();
    }

    @Override
    public Collection<NamedProcedureCallDefinition> getNamedProcedureCallDefinitions() {
        return metadata.getNamedProcedureCallDefinitions();
    }

    @Override
    public ResultSetMappingDefinition getResultSetMapping(final String name) {
        return metadata.getResultSetMapping(name);
    }

    @Override
    public Map<String, ResultSetMappingDefinition> getResultSetMappingDefinitions() {
        return metadata.getResultSetMappingDefinitions();
    }

    @Override
    public TypeDefinition getTypeDefinition(final String typeName) {
        return metadata.getTypeDefinition(typeName);
    }

    @Override
    public Map<String, FilterDefinition> getFilterDefinitions() {
        return metadata.getFilterDefinitions();
    }

    @Override
    public FilterDefinition getFilterDefinition(final String name) {
        return metadata.getFilterDefinition(name);
    }

    @Override
    public FetchProfile getFetchProfile(final String name) {
        return metadata.getFetchProfile(name);
    }

    @Override
    public Collection<FetchProfile> getFetchProfiles() {
        return metadata.getFetchProfiles();
    }

    @Override
    public NamedEntityGraphDefinition getNamedEntityGraph(final String name) {
        return metadata.getNamedEntityGraph(name);
    }

    @Override
    public Map<String, NamedEntityGraphDefinition> getNamedEntityGraphs() {
        return metadata.getNamedEntityGraphs();
    }

    @Override
    public IdentifierGeneratorDefinition getIdentifierGenerator(final String name) {
        return metadata.getIdentifierGenerator(name);
    }

    @Override
    public Collection<Table> collectTableMappings() {
        return metadata.collectTableMappings();
    }

    @Override
    public Map<String, SQLFunction> getSqlFunctionMap() {
        return metadata.getSqlFunctionMap();
    }

    //All methods from org.hibernate.engine.spi.Mapping, the parent of Metadata:

    @Override
    @Deprecated
    public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
        return metadata.getIdentifierGeneratorFactory();
    }

    @Override
    public Type getIdentifierType(final String className) throws MappingException {
        return metadata.getIdentifierType(className);
    }

    @Override
    public String getIdentifierPropertyName(final String className) throws MappingException {
        return metadata.getIdentifierPropertyName(className);
    }

    @Override
    public Type getReferencedPropertyType(final String className, final String propertyName) throws MappingException {
        return metadata.getReferencedPropertyType(className, propertyName);
    }

}
