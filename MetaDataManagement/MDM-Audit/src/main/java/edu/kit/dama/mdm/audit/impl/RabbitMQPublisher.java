/*
 * Copyright 2016 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.mdm.audit.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.InitializationError;
import edu.kit.dama.mdm.audit.interfaces.AbstractAuditPublisher;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class RabbitMQPublisher extends AbstractAuditPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQPublisher.class);

    public static final String HOSTNAME_PROPERTY_KEY = "hostname";
    public static final String EXCHANGE_NAME_KEY = "exchange";

    private String hostname = "localhost";
    private String exchangeName = "audit";

    private Connection connection = null;
    private Channel channel = null;

    @Override
    public boolean initialize() {
        boolean result = false;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            LOGGER.debug("Declaring exchange of type 'topic' in publisher.");
            channel.exchangeDeclare(exchangeName, "topic", true, false, false, null);
            LOGGER.debug("Declaring queue.");
            String queueName = channel.queueDeclare().getQueue();
            LOGGER.debug("Binding queue with name {} to exchange.", queueName);
            channel.queueBind(queueName, exchangeName, "");
            result = true;
        } catch (IOException | TimeoutException ex) {
            LOGGER.error("Failed to initialize RabbitMQPublisher.", ex);
        }
        return result;
    }

    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException ex) {
            }
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException ex) {
            }
        }
    }

    @Override
    public void publish(AuditEvent entry) {
        try {
            LOGGER.debug("Publishing message to channel.");
            if (entry.getCategory() == null || !entry.getCategory().startsWith(exchangeName)) {
                LOGGER.warn("Entry category '{}' does not match exchange topic '{}'. Message will probably be dropped.", entry.getCategory(), exchangeName);
            }
            channel.basicPublish(exchangeName, entry.getCategory(), null, entry.toJson().getBytes());
            LOGGER.debug("Published message to channel.");
        } catch (IOException ex) {
            LOGGER.error("Failed to publish audit event " + entry + " to RabbitMQ exchange.", ex);
        }
    }

    @Override
    public boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException {
        LOGGER.debug("Configurung RabbitMQPublisher");
        hostname = pConfig.getString(HOSTNAME_PROPERTY_KEY, "localhost");
        LOGGER.debug("Publisher hostname set to '{}'", hostname);
        exchangeName = pConfig.getString(EXCHANGE_NAME_KEY, "audit");
        LOGGER.debug("Publisher exchange name set to '{}'", exchangeName);
        return true;
    }

}
