package com.example.login_service.repository;

import com.example.login_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {

    User findByUsername(String username);
    User findByResetToken(String resetToken);
    User findByEmail(String email);

}
