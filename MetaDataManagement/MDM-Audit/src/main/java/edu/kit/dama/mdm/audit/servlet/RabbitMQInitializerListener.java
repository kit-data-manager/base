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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class RabbitMQInitializerListener implements ServletContextListener {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RabbitMQInitializerListener.class);

    private String hostname = "localhost";
    private String exchange = "audit";
    private Connection connection = null;
    private Channel channel = null;
    private boolean CONFIGURED = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Intitializing RabbitMQ.");

        loadConfiguration();
        if (!CONFIGURED) {
            Logger.getLogger("RabbitMQInitializerListener").warning("Skipping initialization of RabbitMQ consumer.");
            return;
        }
        try {
            Logger.getLogger("RabbitMQInitializerListener").info("Intitializing RabbitMQ consumer.");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostname);
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Opening connection to {0}", hostname);
            connection = factory.newConnection();
            Logger.getLogger("RabbitMQInitializerListener").info("Creating new channel.");
            channel = connection.createChannel();
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Declaring topic based exchange with name {0}", exchange);
            channel.exchangeDeclare(exchange, "topic", true);
            String queueName = channel.queueDeclare().getQueue();
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Using queue with name ''{0}''. Binding queue to exchange {1}", new Object[]{queueName, exchange});

            channel.queueBind(queueName, exchange, "audit.#");
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Queue bound to exchange with filter 'audit.#'. Starting consumer.");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    try {
                        Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Handling new audit event.");
                        AuditEvent event = AuditEvent.fromJson(message);
                        Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Submitting event to attached consumers.");
                        for (AbstractAuditConsumer consumer : AuditManager.getInstance().getConsumers()) {
                            consumer.consume(event);
                        }
                        Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Event submitted to all consumers.");
                    } catch (FormatException ex) {
                        Logger.getLogger("RabbitMQInitializerListener").log(Level.SEVERE, "Failed to consume audit event from message '" + message + "'", ex);
                    }
                }
            };
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Start consuming audit message at queue ''{0}'' ", new Object[]{queueName});
            channel.basicConsume(queueName, true, consumer);
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "RabbitMQ consumer successfully initialized.");
        } catch (IOException | TimeoutException ex) {
            Logger.getLogger("RabbitMQInitializerListener").log(Level.SEVERE, "Failed to initialize RabbitMQ audit distributor.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (CONFIGURED) {
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Destroying configured publisher.");
            AuditManager.getInstance().getPublisher().destroy();
            Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Destroying RabbitMQ consumer.");
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
            Logger.getLogger("RabbitMQInitializerListener").log(Level.FINE, "Skipping destruction of unconfigured RabbitMQ consumer.");
        }
    }

    /**
     * Load configuration from XML-File
     */
    private void loadConfiguration() {
        Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Loading RabbitMQ configuration.");
        String publisher = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher[@class]", null);

        Logger.getLogger("RabbitMQInitializerListener").log(Level.INFO, "Checking RabbitMQ publisher class {0}", publisher);
        if (publisher != null && !RabbitMQPublisher.class.getCanonicalName().equals(publisher)) {
            Logger.getLogger("RabbitMQInitializerListener").log(Level.SEVERE, "Configured publisher is not of type {0}. Audit events won't be received.", RabbitMQPublisher.class.getCanonicalName());
        } else {
            hostname = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher.hostname", "localhost");
            exchange = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher.exchange", "audit");
            Logger.getLogger("RabbitMQInitializerListener").log(Level.FINE, "Configuring RabbitMQ consumer with hostname ''{0}'' and exchange ''{1}''", new Object[]{hostname, exchange});
            CONFIGURED = true;
        }
    }
}
