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
package edu.kit.dama.mdm.audit.servlet;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import edu.kit.dama.mdm.audit.exception.FormatException;
import edu.kit.dama.mdm.audit.impl.AuditManager;
import edu.kit.dama.mdm.audit.impl.RabbitMQPublisher;
import edu.kit.dama.mdm.audit.interfaces.AbstractAuditConsumer;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.util.DataManagerSettings;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class RabbitMQInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQInitializerListener.class);
    private String hostname = "localhost";
    private String exchange = "audit";
    private Connection connection = null;
    private Channel channel = null;
    private boolean CONFIGURED = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        loadConfiguration();

        if (!CONFIGURED) {
            LOGGER.warn("Skipping initialization of RabbitMQ consumer.");
            return;
        }

        try {
            LOGGER.debug("Intitializing RabbitMQ consumer.");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostname);
            connection = factory.newConnection();
            channel = connection.createChannel();
            LOGGER.debug("Declaring topic based exchange with name '{}'", exchange);
            channel.exchangeDeclare(exchange, "topic", true);
            String queueName = channel.queueDeclare().getQueue();
            LOGGER.debug("Using queue with name '{}'. Binding queue to exchange.", queueName);
            channel.queueBind(queueName, exchange, "audit.*");
            LOGGER.debug("Queue bound to exchange with filter 'audit.*'. Starting consumer.");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    try {
                        LOGGER.debug("Handling new audit event.");
                        AuditEvent event = AuditEvent.fromJson(message);
                        LOGGER.debug("Submitting event to attached consumers.");
                        for (AbstractAuditConsumer consumer : AuditManager.getInstance().getConsumers()) {
                            consumer.consume(event);
                        }
                        LOGGER.debug("Event submitted to all consumers.");
                    } catch (FormatException ex) {
                        LOGGER.error("Failed to consume audit event from message '" + message + "'", ex);
                    }
                }
            };
            channel.basicConsume(queueName, true, consumer);
            LOGGER.debug("RabbitMQ consumer successfully initialized.");
        } catch (IOException | TimeoutException ex) {
            LOGGER.error("Failed to initialize RabbitMQ audit distributor.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (CONFIGURED) {
            LOGGER.debug("Destroying configured publisher.");
            AuditManager.getInstance().getPublisher().destroy();
            LOGGER.debug("Destroying RabbitMQ consumer.");
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException ex) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ex) {
                }
            }
        } else {
            LOGGER.debug("Skipping destruction of unconfigured RabbitMQ consumer.");
        }
    }

    /**
     * Load configuration from XML-File
     */
    private void loadConfiguration() {
        String publisher = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher[@class]", null);

        if (publisher != null && !RabbitMQPublisher.class.getCanonicalName().equals(publisher)) {
            LOGGER.warn("Configured publisher is not of type " + RabbitMQPublisher.class.getCanonicalName() + ".");
        } else {
            hostname = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher.hostname", "localhost");
            exchange = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher.exchange", "audit");
            LOGGER.debug("Configuring RabbitMQ consumer with hostname '{}' and exchange '{}'", hostname, exchange);
            CONFIGURED = true;
        }
    }
}
