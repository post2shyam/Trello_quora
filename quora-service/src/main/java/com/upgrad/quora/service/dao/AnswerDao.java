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
     * @param answerUuid
     * @return
     */
    public AnswerEntity getAnswerByUuId(final String answerUuid) {
        try {
            return entityManager.createNamedQuery("answerFromUuid", AnswerEntity.class).setParameter("answerUuid", answerUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
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
     * @param questionUuid
     * @return
     */
    public List<AnswerEntity> getAllAnswersToQuestion(final String questionUuid) {
        try {
            return entityManager.createNamedQuery("allAnswersToQuestion", AnswerEntity.class).setParameter("questionUuid", questionUuid).getResultList();
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