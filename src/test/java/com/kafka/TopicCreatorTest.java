package com.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicCreatorTest {

    @Mock
    private AdminClient adminClient;

    private TopicCreator topicCreator;

    @BeforeEach
    void setUp() {
        topicCreator = new TopicCreator(adminClient, (short) 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTopics_createsAllConfiguredTopics() throws Exception {
        CreateTopicsResult result = mock(CreateTopicsResult.class);
        KafkaFuture<Void> future = mock(KafkaFuture.class);
        when(future.get()).thenReturn(null);
        when(result.all()).thenReturn(future);
        when(adminClient.createTopics(any())).thenReturn(result);

        Map<String, Integer> topics = new HashMap<>();
        topics.put("item", 1);
        topics.put("order", 2);
        topics.put("shipment", 1);
        topicCreator.setTopics(topics);

        topicCreator.createTopics();

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(adminClient).createTopics(captor.capture());

        Collection<NewTopic> capturedTopics = captor.getValue();
        assertThat(capturedTopics).hasSize(3);

        Map<String, Integer> capturedMap = new HashMap<>();
        for (NewTopic nt : capturedTopics) {
            capturedMap.put(nt.name(), nt.numPartitions());
        }
        assertThat(capturedMap).containsEntry("item", 1);
        assertThat(capturedMap).containsEntry("order", 2);
        assertThat(capturedMap).containsEntry("shipment", 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTopics_whenKafkaUnavailable_doesNotThrow() throws Exception {
        CreateTopicsResult result = mock(CreateTopicsResult.class);
        KafkaFuture<Void> future = mock(KafkaFuture.class);
        when(future.get()).thenThrow(new RuntimeException("Broker unavailable"));
        when(result.all()).thenReturn(future);
        when(adminClient.createTopics(any())).thenReturn(result);

        Map<String, Integer> topics = new HashMap<>();
        topics.put("item", 1);
        topicCreator.setTopics(topics);

        assertThatCode(() -> topicCreator.createTopics()).doesNotThrowAnyException();
    }
}
