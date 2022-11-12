source runner_env.sh

fuser -k 10501/tcp || true

java -jar production-portfolio/libs/portfolio-0.0.1-SNAPSHOT.jar --spring.config.additional-location=classpath:application.production.properties