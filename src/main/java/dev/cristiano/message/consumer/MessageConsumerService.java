package dev.cristiano.message.consumer;

import java.io.InputStream;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import dev.cristiano.api.v1.UsertaskCompletionV1;

@Component
public class MessageConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerService.class);
    private static final String TOPIC_NAME = "usertask.completions";
    private static final String SUBSCRIPTION_NAME_V1 = "customer-management-camunda-v1";
    private static final String SCHEMA_PATH_v1 = "schema/v1/usertask-completion-v1.json";

    @JmsListener(destination = TOPIC_NAME, containerFactory = "topicJmsListenerContainerFactory", subscription = SUBSCRIPTION_NAME_V1)
    public void receiveCompletionMessageV1(UsertaskCompletionV1 usertaskCompletionV1) {
        Set<ValidationMessage> validationErrors = validateMessageV1(usertaskCompletionV1, SCHEMA_PATH_v1);
        if (validationErrors != null && validationErrors.size() > 0) {
            LOGGER.info("Message v1 invalid: {}", validationErrors);
        } else {
            LOGGER.info("Received message v1 valid");
            LOGGER.info("usertaskId: {}", usertaskCompletionV1.getUsertaskId());
            LOGGER.info("customerId: {}", usertaskCompletionV1.getCustomerId());
            LOGGER.info("createdBy: {}", usertaskCompletionV1.getCreatedBy());
            LOGGER.info("outputData: {}", usertaskCompletionV1.getOutputData());
            LOGGER.info("correlationData: {}", usertaskCompletionV1.getCorrelationData());
        }
    }

    private Set<ValidationMessage> validateMessageV1(UsertaskCompletionV1 usertaskCompletionV1, String schemaPath) {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012);

        InputStream jsonSchemaAsInputStream = this.getClass().getClassLoader()
                .getResourceAsStream(schemaPath);

        LOGGER.info("Classpath: {}", this.getClass().getClassLoader().toString());
        JsonSchema jsonSchema = jsonSchemaFactory.getSchema(jsonSchemaAsInputStream);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(usertaskCompletionV1, JsonNode.class);
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        return errors;
    }

}
