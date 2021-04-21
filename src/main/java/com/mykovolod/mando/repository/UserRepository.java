package com.mykovolod.mando.repository;

import com.mykovolod.mando.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
