package org.example.thuan_security.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.UserRepositoryCustom;
import org.example.thuan_security.request.UserSearchRequest;
import org.example.thuan_security.utils.SqlUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Users> search(UserSearchRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select e from Users e " + createWhereQuery(request, values) + createOrderQuery(request.getSortBy());
        log.info(sql);
        Query query = entityManager.createQuery(sql, Users.class);
        values.forEach(query::setParameter);
        query.setFirstResult((request.getPage() ) * request.getSize());
        query.setMaxResults(request.getSize());
        log.info(String.valueOf(query.getResultList()));
        return query.getResultList();
    }

    @Override
    public Long count(UserSearchRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(e) from Users e " + createWhereQuery(request, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(UserSearchRequest request, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        sql.append(" where e.deleted = false");
        if (StringUtils.isNotBlank(request.getKeyword())) {
            sql.append(
                    " and (   lower(e.email) like :keyword" +
                            " or lower(e.fullName) like :keyword"
                             + " OR lower(to_char(e.createdAt, 'YYYY-MM-DD HH24:MI:SS')) LIKE :keyword"
                            + " or lower(e.userId) like :keyword ) ");
            values.put("keyword", SqlUtils.encodeKeyword(request.getKeyword()));
        }

        if (StringUtils.isNotBlank(request.getEmail())) {
            log.info(request.getEmail());
            sql.append(" and lower(e.email) like :email ");
            values.put("email", SqlUtils.encodeKeyword(request.getEmail()));
        }
        if (StringUtils.isNotBlank(request.getCreateAt())) {
            sql.append(" and lower(to_char(e.createdAt, 'YYYY-MM-DD HH24:MI:SS')) LIKE :createAt ");
            values.put("createAt", SqlUtils.encodeKeyword(request.getCreateAt()));
        }


        return sql.toString();
    }

    public StringBuilder createOrderQuery(String sortBy) {
        StringBuilder hql = new StringBuilder(" ");
        if (org.springframework.util.StringUtils.hasLength(sortBy)) {
            hql.append(" order by e.").append(sortBy.replace(".", " "));
        } else {
            hql.append(" order by e.lastModifiedAt desc ");
        }
        return hql;
    }
}
