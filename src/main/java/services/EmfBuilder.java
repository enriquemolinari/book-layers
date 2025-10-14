package services;

import data.entities.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;

public class EmfBuilder {
    public static final String DB_USER = "app";
    public static final String DB_PWD = "app";
    public static final String IN_MEMORY_DB_URL = "jdbc:derby:memory:cinema;create=true";
    public static final String CLIENT_DB_URL = "jdbc:derby://%s:%s/%s;create=true";
    public static final String DO_NOTHING_WITH_SCHEMA = "none";
    public static final String DROP_AND_CREATE_SCHEMA = "drop-and-create";
    public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    public static final String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
    public static final String HIBERNATE_HIGHLIGHT_SQL = "hibernate.highlight_sql";
    private PersistenceConfiguration config;

    public EmfBuilder() {
        config = new PersistenceConfiguration("cinemaConfig")
                .managedClass(Actor.class)
                .managedClass(Email.class)
                .managedClass(LoginAudit.class)
                .managedClass(Movie.class)
                .managedClass(Theater.class)
                .managedClass(Person.class)
                .managedClass(Rating.class)
                .managedClass(Sale.class)
                .managedClass(ShowSeat.class)
                .managedClass(ShowTime.class)
                .managedClass(Theater.class)
                .managedClass(User.class)
                .managedClass(UserRate.class)
                .property(PersistenceConfiguration.JDBC_USER, DB_USER)
                .property(PersistenceConfiguration.JDBC_PASSWORD, DB_PWD)
                .property(PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION,
                        DO_NOTHING_WITH_SCHEMA);
    }

    public EmfBuilder memory() {
        config.property(PersistenceConfiguration.JDBC_URL,
                IN_MEMORY_DB_URL);
        return this;
    }

    public EmfBuilder debugQueries() {
        config.property(HIBERNATE_SHOW_SQL, true);
        config.property(HIBERNATE_FORMAT_SQL, true);
        config.property(HIBERNATE_HIGHLIGHT_SQL, true);
        return this;
    }

    public EmfBuilder clientAndServer(String databaseName, String databaseServer, String port) {
        config.property(PersistenceConfiguration.JDBC_URL,
                String.format(CLIENT_DB_URL, databaseServer, port, databaseName));
        return this;
    }

    public EmfBuilder withDropAndCreateDDL() {
        config.property(PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION,
                DROP_AND_CREATE_SCHEMA);
        return this;
    }

    public EntityManagerFactory build() {
        return config.createEntityManagerFactory();
    }
}

