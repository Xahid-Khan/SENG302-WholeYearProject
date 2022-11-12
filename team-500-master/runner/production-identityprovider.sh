source runner_env.sh
fuser -k 10500/tcp || true
java -jar production-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar --spring.config.additional-location=classpath:application.production.properties
