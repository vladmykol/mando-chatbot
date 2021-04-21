package com.mykovolod.mando.repository;

import com.mykovolod.mando.conts.RoleEnum;
import com.mykovolod.mando.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {
    Role findByRole(RoleEnum role);
}
