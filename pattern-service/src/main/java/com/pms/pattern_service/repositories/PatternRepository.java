package com.pms.pattern_service.repositories;

import com.pms.pattern_service.entities.Pattern;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatternRepository extends MongoRepository<Pattern, String> {
}
