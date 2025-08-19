package com.example.user_service.repository;

import com.example.user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User ,String> {

    User findByUsername(String username);
}
