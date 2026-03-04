package com.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties
public class TopicCreator {
    private final AdminClient adminClient;
    private final short replicationFactor;
    private Map<String, Integer> topics = new HashMap<>();

    @Autowired
    public TopicCreator(AdminClient adminClient,
                        @Value("${kafka.replication-factor:3}") short replicationFactor) {
        this.adminClient = adminClient;
        this.replicationFactor = replicationFactor;
    }

    public void setTopics(Map<String, Integer> topics) {
        this.topics = topics;
    }

    @PostConstruct
    public void createTopics() {
        List<NewTopic> list = new ArrayList<>();
        topics.forEach((name, parts) ->
                list.add(new NewTopic(name, parts, replicationFactor)));
        try {
            adminClient.createTopics(list).all().get();
        } catch (Exception e) {
            // log and continue — broker may not be available at startup
            System.err.println("Topic creation failed (broker may be unavailable): " + e.getMessage());
        }
    }
}
