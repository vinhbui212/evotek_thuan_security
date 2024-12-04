package org.example.thuan_security.repository;

import org.example.thuan_security.model.Users;
import org.example.thuan_security.request.UserSearchRequest;

import java.util.List;

public interface UserRepositoryCustom {
    List<Users> search(UserSearchRequest request);

    Long count(UserSearchRequest request);
}
