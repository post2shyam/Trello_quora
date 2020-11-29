package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    public AnswerEntity getAnswerByUuId(final String uuid) {
        try {
            return entityManager.createNamedQuery("AnswerFromUuid", AnswerEntity.class).setParameter("answerUuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Boolean isUserOwnerOfAnswer(String answerUuid, String userUuid) {

        try {
            AnswerEntity answerEntity = entityManager.createNamedQuery("validateOwnership", AnswerEntity.class).setParameter("answerUuid", answerUuid).setParameter("userUuid", userUuid).getSingleResult();
            if (answerEntity == null) {
                return false;
            }
            else {
                return true;
            }
        }catch (NoResultException nre)
        {
            return false;
        }
    }

    public AnswerEntity deleteAnswer(AnswerEntity answerEntity)
    {
        entityManager.remove(answerEntity);
        return answerEntity;
    }

    public AnswerEntity editAnswer(AnswerEntity answerEntity)
    {
        return entityManager.merge(answerEntity);
    }

    public UserAuthEntity getUserAuthToken(final String accesstoken) {
        try {
            return entityManager.createNamedQuery("userAuthByAccessToken", UserAuthEntity.class).setParameter("accessToken", accesstoken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}