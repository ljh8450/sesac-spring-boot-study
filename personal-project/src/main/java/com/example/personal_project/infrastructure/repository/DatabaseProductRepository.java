package com.example.personal_project.infrastructure.repository;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.domain.item.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class DatabaseProductRepository implements ItemRepository {

    private List<CollectionItem> items;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseProductRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public CollectionItem add(CollectionItem collectionItem) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);

        namedParameterJdbcTemplate.update("INSERT INTO products (name, price, stock) VALUES (:name, :price, :stock)", namedParameter, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("저장된 상품의 ID를 가져오지 못했습니다.");
        }
        Long generatedId = key.longValue();
        CollectionItem generatedItem = collectionItem.withId(generatedId);

        return generatedItem;
    }

    @Override
    public List<CollectionItem> findAll() {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM products",
                new DataClassRowMapper<>(CollectionItem.class)
        );
    }

    @Override
    public CollectionItem findById(Long id) {
        SqlParameterSource namedParameter = new MapSqlParameterSource("id", id);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                """
                SELECT id, name, price, stock,
                       'MANUAL' AS source_type,
                       NULL AS external_id
                FROM products
                WHERE id = :id
                """,
                namedParameter, new DataClassRowMapper<>(CollectionItem.class));
        } catch (EmptyResultDataAccessException exception) {
            throw new NoSuchElementException("상품을 찾을 수 없습니다. ID: " + id);
        }
    }

    @Override
    public boolean existsBySourceTypeAndExternalId(SourceType sourceType, Long externalId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("sourceType", sourceType.name())
                .addValue("externalId", externalId);

        Long count = namedParameterJdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM products
                WHERE source_type = :sourceType
                    AND external_id = :externalId
                """,
                namedParameters,
                Long.class
        );
        return count != null && count > 0;
    }

    public CollectionItem update(CollectionItem collectionItem) {
        SqlParameterSource namedParameter = new BeanPropertySqlParameterSource(collectionItem);

        namedParameterJdbcTemplate.update(
                """
                       UPDATE products
                       SET name=:name,
                        price=:price,
                        stock=:stock
                       WHERE id=:id
                       """,
                namedParameter
        );

        return collectionItem;
    }
}
