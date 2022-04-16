package com.mykovolod.mando.config;

import com.mykovolod.mando.conts.RoleEnum;
import com.mykovolod.mando.entity.Role;
import com.mykovolod.mando.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.time.Duration;
import java.time.Instant;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMongoAuditing
public class MongoDbConfig {
    private final MongoTemplate mongoTemplate;
    private final MongoConverter mongoConverter;

    @EventListener(ContextRefreshedEvent.class)
    public void autoIndexCreation() {
        Instant start = Instant.now();
        var mappingContext = (MongoMappingContext) mongoConverter.getMappingContext();
        var resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        // consider only entities that are annotated with @Document
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Document.class))
                .forEach(it -> {
                    var indexOps = mongoTemplate.indexOps(it.getType());
                    resolver.resolveIndexFor(it.getType()).forEach(indexOps::ensureIndex);
                });
        Duration timeElapsed = Duration.between(start, Instant.now());
        log.info("Mongo DB index creation on startup took: {}", DurationFormatUtils.formatDurationHMS(timeElapsed.toMillis()));
    }

    @Autowired
    public void setMapKeyDotReplacement(MappingMongoConverter mongoConverter) {
        mongoConverter.setMapKeyDotReplacement("#");
    }

    @Bean
    CommandLineRunner presetUserRoles(RoleRepository roleRepository) {
        return args -> {
            for (RoleEnum roleEnum : RoleEnum.values()) {
                Role existingRole = roleRepository.findByRole(roleEnum);
                if (existingRole == null) {
                    Role newRole = new Role();
                    newRole.setRole(roleEnum);
                    roleRepository.save(newRole);
                }
            }
        };
    }
}
