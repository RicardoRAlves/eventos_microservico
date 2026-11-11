package com.br.capoeira.eventos.event_api.seed;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class CategorySeedLock {

    static final String LOCK_ID = "category-seed";
    static final String COLLECTION = "application_lock";

    private final MongoTemplate mongoTemplate;

    public CategorySeedLock(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean tryAcquire(String ownerId, Duration leaseDuration) {
        Instant now = Instant.now();
        Query query = Query.query(new Criteria().andOperator(
                Criteria.where("_id").is(LOCK_ID),
                new Criteria().orOperator(
                        Criteria.where("expiresAt").lte(Date.from(now)),
                        Criteria.where("ownerId").is(ownerId)
                )
        ));
        Update update = new Update()
                .set("ownerId", ownerId)
                .set("expiresAt", Date.from(now.plus(leaseDuration)));

        try {
            Document lock = mongoTemplate.findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().upsert(true).returnNew(true),
                    Document.class,
                    COLLECTION
            );
            return lock != null && ownerId.equals(lock.getString("ownerId"));
        } catch (org.springframework.dao.DuplicateKeyException | DuplicateKeyException exception) {
            return false;
        }
    }

    public boolean renew(String ownerId, Duration leaseDuration) {
        Query query = Query.query(Criteria.where("_id").is(LOCK_ID).and("ownerId").is(ownerId));
        Update update = new Update().set("expiresAt", Date.from(Instant.now().plus(leaseDuration)));
        Document lock = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Document.class,
                COLLECTION
        );
        return lock != null && ownerId.equals(lock.getString("ownerId"));
    }

    public boolean release(String ownerId) {
        Query query = Query.query(Criteria.where("_id").is(LOCK_ID).and("ownerId").is(ownerId));
        DeleteResult result = mongoTemplate.remove(query, COLLECTION);
        return result.getDeletedCount() == 1;
    }
}
