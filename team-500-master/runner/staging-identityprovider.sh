source runner_env.sh
fuser -k 9500/tcp || true
java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar --spring.config.additional-location=classpath:application.staging.properties
