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

## Quick Start
Before starting the application, make sure you have Docker running on your machine
and ports 5432 and 8080 are not taken by any other service or container.

To deploy the entire stack (database + the application),
simply run the deployment script from the root directory:
```shell
# Make the script executable (first time only)
chmod +x run.sh

# Run the deployment
./run.sh
```
The app will be available via http://localhost:8080/

## Testing
In order to run tests, make sure Docker is running on your machine.
This is required for Testcontainers to run a full Postgres database supporting tests. 
Run this command from the root directory:
```shell
./gradlew test
```

## Additional configuration
If you want the database wiped every time you run the app, simply set
the RESET_DATABASE in `run.sh` to `true`. This will remove all the volumes
previously created for the app.
