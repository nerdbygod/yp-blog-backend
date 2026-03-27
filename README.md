# Readme

## Prerequisites
- Java 21+
- Docker 24.0.5 and above
- bash shell (for running deployment .sh script)

## Configuration
In order for the application to start properly, it requires a database.
The only variables you need to configure are POSTGRES_PASSWORD and DB_PASSWORD
in `docker-compose.yml` file. Make sure they have the same values.
The rest of the database configuration (the database user, connection URL etc.) is already done.
If you want, you can configure you own user and database name, but make sure they're consistent
across the `init-db.sql` and `docker-compose.yml` files.

### Extra notes
You may want to configure the `Dockerfile` to copy the JAR from
one of the target directories into the Docker container based on
your preferred build system. It's preconfigured to use Maven as the
default build system.

## Quick Start
Before starting the application, make sure you have Docker running on your machine
and ports 5432 and 8080 are not taken by any other service or container.

It is now possible to build the app via both Gradle and Maven.

To deploy the entire stack (database + the application),
simply run one of the following deployment scripts from the root directory.

Gradle:
```shell
# Make the script executable (first time only)
chmod +x gradle-run.sh

# Run the deployment
./gradle-run.sh
```

Maven:
```shell
chmod +x maven-run.sh

./maven-run.sh
```

The app will be available via http://localhost:8080/

## Testing
In order to run tests, make sure Docker is running on your machine.
This is required for Testcontainers to run a full Postgres database supporting tests. 
Run one of these commands from the root directory

Gradle:
```shell
./gradlew test
```

or Maven:
```shell
./mvnw verify
```

## Additional configuration
If you want the database wiped every time you run the app, simply set
the RESET_DATABASE in `maven-run.sh` or `gradle-run.sh` (depending on your preferred build system)
to `true`. This will remove all the volumes previously created for the app.
