package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @param answerEntity
     * @return
     */
    public AnswerEntity createAnswer(final AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    /**
     * @param uuid
     * @return
     */
    public AnswerEntity getAnswerByUuId(final String uuid) {
        try {
            return entityManager.createNamedQuery("AnswerFromUuid", AnswerEntity.class).setParameter("answerUuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * @param answerUuid
     * @param userUuid
     * @return
     */
    public Boolean isUserOwnerOfAnswer(final String answerUuid, final String userUuid) {
        try {
            AnswerEntity answerEntity = entityManager.createNamedQuery("validateOwnership", AnswerEntity.class).setParameter("answerUuid", answerUuid).setParameter("userUuid", userUuid).getSingleResult();
            if (answerEntity == null) {
                return false;
            } else {
                return true;
            }
        } catch (NoResultException nre) {
            return false;
        }
    }

    /**
     * @param answerEntity
     * @return
     */
    public AnswerEntity deleteAnswer(final AnswerEntity answerEntity) {
        entityManager.remove(answerEntity);
        return answerEntity;
    }

    /**
     * @param answerEntity
     * @return
     */
    public AnswerEntity editAnswer(final AnswerEntity answerEntity) {
        return entityManager.merge(answerEntity);
    }

    /**
     * @param uuid
     * @return
     */
    public List<AnswerEntity> getAllAnswersToQuestion(final String uuid) {
        try {
            return entityManager.createNamedQuery("allAnswersToQuestion", AnswerEntity.class).setParameter("questionUuid", uuid).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * @param accessToken
     * @return
     */
    public UserAuthEntity getUserAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("userAuthByAccessToken", UserAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}