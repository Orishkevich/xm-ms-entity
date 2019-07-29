package com.icthh.xm.ms.entity.service;

import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.lep.LogicExtensionPoint;
import com.icthh.xm.commons.lep.spring.LepService;
import com.icthh.xm.commons.permission.annotation.FindWithPermission;
import com.icthh.xm.commons.permission.repository.PermittedRepository;
import com.icthh.xm.ms.entity.domain.Attachment;
import com.icthh.xm.ms.entity.domain.XmEntity;
import com.icthh.xm.ms.entity.domain.spec.AttachmentSpec;
import com.icthh.xm.ms.entity.repository.AttachmentRepository;
import com.icthh.xm.ms.entity.repository.XmEntityRepository;
import com.icthh.xm.ms.entity.repository.search.PermittedSearchRepository;
import com.icthh.xm.ms.entity.service.impl.StartUpdateDateGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Service Implementation for managing Attachment.
 */
@Service
@LepService(group = "service.attachment")
@Transactional
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_ATTACHMENTS = 100;

    private final AttachmentRepository attachmentRepository;

    private final PermittedRepository permittedRepository;

    private final PermittedSearchRepository permittedSearchRepository;

    private final StartUpdateDateGenerationStrategy startUpdateDateGenerationStrategy;

    private final XmEntityRepository xmEntityRepository;

    private final XmEntitySpecService xmEntitySpecService;

    /**
     * Save a attachment.
     *
     * @param attachment the entity to save
     * @return the persisted entity
     */
    @LogicExtensionPoint("Save")
    public Attachment save(Attachment attachment) {

        startUpdateDateGenerationStrategy.preProcessStartDate(attachment,
                                                              attachment.getId(),
                                                              attachmentRepository,
                                                              Attachment::setStartDate,
                                                              Attachment::getStartDate);

        XmEntity entity = xmEntityRepository.getOne(attachment.getXmEntity().getId());

        AttachmentSpec spec = getSpec(entity, attachment);

        //check only for addingNew
        if (attachment.getId() == null && spec.getMax() != null) {
            //forbid to add element if spec.max = 0
            assertZeroRestriction(spec);
            //forbid to add element if spec.max <= addedSize
            assertLimitRestriction(spec, entity);
        }

        attachment.setXmEntity(entity);

        if (attachment.getContent() != null) {
            byte[] content = attachment.getContent().getValue();
            attachment.setContentChecksum(DigestUtils.sha256Hex(content));
        }
        return attachmentRepository.save(attachment);
    }

    /**
     *  Get all the attachments.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("ATTACHMENT.GET_LIST")
    public List<Attachment> findAll(String privilegeKey) {
        return permittedRepository.findAll(Attachment.class, privilegeKey);
    }

    /**
     *  Get one attachment by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Attachment findOneWithContent(Long id) {
        return attachmentRepository.findById(id)
            .map(att -> {
                Hibernate.initialize(att.getContent());
                return att;
            }).orElse(null);
    }

    /**
     * Get one attachment by id with content.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Attachment findOne(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  attachment by id.
     *
     *  @param id the id of the entity
     */
    @LogicExtensionPoint("Delete")
    public void delete(Long id) {
        attachmentRepository.deleteById(id);
    }

    /**
     * Search for the attachment corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    @FindWithPermission("ATTACHMENT.SEARCH")
    public List<Attachment> search(String query, String privilegeKey) {
        return permittedSearchRepository.search(query, Attachment.class, privilegeKey);
    }

    protected AttachmentSpec getSpec(XmEntity entity, Attachment attachment) {
        return xmEntitySpecService
            .findAttachment(entity.getTypeKey(), attachment.getTypeKey())
            .orElseThrow(
                () -> new IllegalArgumentException("Spec.Attachment not found for entity type key " + entity.getTypeKey()
                    + " and function key: " + attachment.getTypeKey())
            );
    }

    protected void assertZeroRestriction(AttachmentSpec spec) {
        if (Integer.valueOf(0).equals(spec.getMax())) {
            throw new BusinessException("Spec for " + spec.getKey() + " allows to add " + spec.getMax() + " elements");
        }
    }

    protected void assertLimitRestriction(AttachmentSpec spec, XmEntity entity) {
        Predicate<Attachment> filterByType = (Attachment item) -> spec.getKey().equals(item.getTypeKey());
        Stream<Attachment> attachmentStream = entity.getAttachments().stream().filter(filterByType);

        if (attachmentStream.count() >= spec.getMax()) {
            throw new BusinessException("Spec for " + spec.getKey() + " allows to add " + spec.getMax() + " elements");
        }
    }

}
