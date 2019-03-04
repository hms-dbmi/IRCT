FROM jboss/wildfly:10.1.0.Final AS production

LABEL maintainer="avillach_lab_developers@googlegroups.com"

# mysql database
ENV IRCT_DB_HOST localhost
ENV IRCT_DB_PORT 3306
ENV IRCT_DB_CONNECTION_USER root
ENV IRCT_DB_PASSWORD password

# JWT Token
ENV AUTH0_DOMAIN domain
ENV CLIENT_ID dummyid
ENV CLIENT_SECRET dummysecret
ENV IRCT_USER_FIELD email

# verification / introspection
ENV VERIFY_USER_METHOD sessionFilter
ENV TOKEN_INTROSPECTION_URL http://localhost
ENV TOKEN_INTROSPECTION_TOKEN dummytoken

# result data folder
ENV RESULT_DATA_FOLDER /scratch/irct
ENV IRCT_KEY_TIMEOUT_IN_MINUTES 720
ENV WHITELIST_ENABLED false
ENV WHITELIST_CONFIG_FILE /whitelist/whitelist.json

# copy modules
# NOTE: wildfly-9.0.1.Final is a hard-coded assumption in maven build - Andre
COPY IRCT-CL/wildfly-9.0.1.Final/modules/system/layers/base/com/sql/mysql/main/* /modules/

# PIC-SURE event UMLS Synonym uses Oracle jdbc driver
# see https://github.com/hms-dbmi/PIC-SURE-resources
COPY IRCT-CL/wildfly-9.0.1.Final/modules/system/layers/base/com/oracle/main/* /modules/

# Copy whitelist.json
COPY IRCT-API/src/main/resources/whitelist.json.sample $WHITELIST_CONFIG_FILE

# Copy standalone.xml
COPY IRCT-CL/src/main/resources/wildfly-configuration/standalone.xml wildfly/standalone/configuration/

# Copy war file
COPY IRCT-CL/target/IRCT-CL.war wildfly/standalone/deployments/

# root required to create default scratch directory - Andre
USER root

# install modules
RUN wildfly/bin/jboss-cli.sh --command="module add --name=com.sql.mysql \
    --resources=/modules/mysql-connector-java-5.1.38.jar --dependencies=javax.api" \
    && wildfly/bin/jboss-cli.sh --command="module add --name=com.oracle \
    --resources=/modules/ojdbc6-11.2.0.3.jar --dependencies=javax.api,javax.transaction.api" \
    #
    # IRCT scratch directory
    && mkdir -p $RESULT_DATA_FOLDER \
    && chmod a+rw $RESULT_DATA_FOLDER \
    # make whitelist accessible to jboss
    && chmod a+rw $WHITELIST_CONFIG_FILE

USER jboss

ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
