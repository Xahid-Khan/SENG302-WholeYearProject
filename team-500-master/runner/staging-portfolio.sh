source runner_env.sh

fuser -k 9501/tcp || true

java -jar staging-portfolio/libs/portfolio-0.0.1-SNAPSHOT.jar \
    --spring.config.additional-location=classpath:application.staging.properties