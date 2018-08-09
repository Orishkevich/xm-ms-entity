package com.icthh.xm.ms.entity.service.impl;

import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_AUTH_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.commons.tenant.spring.config.TenantContextConfiguration;
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.entity.EntityApp;
import com.icthh.xm.ms.entity.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.entity.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.entity.domain.Attachment;
import com.icthh.xm.ms.entity.domain.Calendar;
import com.icthh.xm.ms.entity.domain.Comment;
import com.icthh.xm.ms.entity.domain.Event;
import com.icthh.xm.ms.entity.domain.FunctionContext;
import com.icthh.xm.ms.entity.domain.Link;
import com.icthh.xm.ms.entity.domain.Location;
import com.icthh.xm.ms.entity.domain.Rating;
import com.icthh.xm.ms.entity.domain.Tag;
import com.icthh.xm.ms.entity.domain.Vote;
import com.icthh.xm.ms.entity.domain.XmEntity;
import com.icthh.xm.ms.entity.repository.XmEntityRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    EntityApp.class,
    SecurityBeanOverrideConfiguration.class,
    WebappTenantOverrideConfiguration.class,
    TenantContextConfiguration.class
})
public class DeleteEntityIntTest {

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private XmEntityServiceImpl xmEntityService;

    @Autowired
    private XmEntityRepository xmEntityRepository;

    @Mock
    private XmAuthenticationContextHolder authContextHolder;

    @Mock
    private XmAuthenticationContext context;

    @Before
    public void beforeTransaction() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
        MockitoAnnotations.initMocks(this);
        when(authContextHolder.getContext()).thenReturn(context);
        when(context.getRequiredUserKey()).thenReturn("userKey");

        lepManager.beginThreadContext(ctx -> {
            ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
        });
    }


    @After
    public void afterTest() {
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
    }

    private XmEntity createXmEntity() {
        XmEntity xmEntity = new XmEntity().key(randomUUID().toString()).typeKey("TEST_DELETE");
        xmEntity.name("name")
            .functionContexts(asSet(
                new FunctionContext().key("1").typeKey("A").xmEntity(xmEntity),
                new FunctionContext().key("2").typeKey("A").xmEntity(xmEntity),
                new FunctionContext().key("3").typeKey("A").xmEntity(xmEntity)
            ))
            .attachments(asSet(
                new Attachment().typeKey("A").name("1"),
                new Attachment().typeKey("A").name("2"),
                new Attachment().typeKey("A").name("3")
            ))
            .calendars(asSet(
                new Calendar().typeKey("A").name("1").events(asSet(
                    new Event().typeKey("A").title("1"),
                    new Event().typeKey("A").title("2")
                )),
                new Calendar().typeKey("A").name("2").events(asSet(
                    new Event().typeKey("A").title("3"),
                    new Event().typeKey("A").title("4")
                ))
            ))
            .locations(asSet(
                new Location().typeKey("A").name("1"),
                new Location().typeKey("A").name("2")
            ))
            .ratings(asSet(
                new Rating().typeKey("A").votes(asSet(
                    new Vote().message("1").value(1.1).userKey("1"),
                    new Vote().message("2").value(2.1).userKey("2")
                ))
            ))
            .tags(asSet(
                new Tag().typeKey("A").name("1"),
                new Tag().typeKey("A").name("2")
            ))
            .comments(asSet(
                new Comment().message("1").userKey("1"),
                new Comment().message("2").userKey("1")
            ));
        return xmEntity;
    }

    @Test
    public void testDelete() {

        XmEntity entity = xmEntityService.save(createXmEntity());

        xmEntityService.delete(entity.getId());
    }

    @Test
    public void testDeleteLink() {

        XmEntity entity = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_DELETE"));
        XmEntity breakLink = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_SEARCH"));
        XmEntity cascadeDeleteLink = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_NEW"));

        Link link = new Link();
        entity.addTargets(link);

        link.setTypeKey("breakLinks");
        link.setTarget(breakLink);
        link.setSource(entity);

        Link link2 = new Link();
        link2.setTypeKey("cascadeDeleteLinks");
        link2.setTarget(cascadeDeleteLink);
        entity.addTargets(link2);

        xmEntityService.save(entity);

        XmEntity cascadeBreakSubLinks = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_SEARCH"));
        XmEntity cascadeDeleteSubLinks = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_NEW"));

        cascadeDeleteLink.addTargets(new Link().typeKey("cascadeBreakSubLinks").target(cascadeBreakSubLinks));
        cascadeDeleteLink.addTargets(new Link().typeKey("cascadeDeleteSubLinks").target(cascadeDeleteSubLinks));

        xmEntityService.save(cascadeDeleteLink);

        xmEntityService.delete(entity.getId());

        assertThat(xmEntityRepository.exists(entity.getId())).isFalse();

        assertThat(xmEntityRepository.exists(breakLink.getId())).isTrue();
        assertThat(xmEntityRepository.exists(cascadeDeleteLink.getId())).isFalse();

        assertThat(xmEntityRepository.exists(cascadeBreakSubLinks.getId())).isTrue();
        assertThat(xmEntityRepository.exists(cascadeDeleteSubLinks.getId())).isFalse();

        xmEntityService.delete(breakLink.getId());
        xmEntityService.delete(cascadeBreakSubLinks.getId());
    }


    private <T> Set<T> asSet(T... elements) {
        Set<T> set = new HashSet<>();
        for(T element: elements) {
            set.add(element);
        }
        return set;
    }


    @Test
    @SneakyThrows
    public void testDeleteLinkInner() {

        XmEntity parentEntity = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_DELETE_PARENT"));

        XmEntity entity = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_DELETE"));
        XmEntity breakLink = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_SEARCH"));
        XmEntity cascadeDeleteLink = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_NEW"));


        Link testLink = new Link();
        parentEntity.addTargets(testLink);

        testLink.setTypeKey("testLink");
        testLink.setTarget(entity);
        testLink.setSource(parentEntity);
        xmEntityService.save(parentEntity);

        Link link = new Link();
        entity.addTargets(link);

        link.setTypeKey("breakLinks");
        link.setTarget(breakLink);
        link.setSource(entity);

        Link link2 = new Link();
        link2.setTypeKey("cascadeDeleteLinks");
        link2.setTarget(cascadeDeleteLink);
        entity.addTargets(link2);

        xmEntityService.save(entity);

        XmEntity cascadeBreakSubLinks = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_SEARCH"));
        XmEntity cascadeDeleteSubLinks = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_LIFECYCLE_LINK_NEW"));

        cascadeDeleteLink.addTargets(new Link().typeKey("cascadeBreakSubLinks").target(cascadeBreakSubLinks));
        cascadeDeleteLink.addTargets(new Link().typeKey("cascadeDeleteSubLinks").target(cascadeDeleteSubLinks));

        xmEntityService.save(cascadeDeleteLink);

        xmEntityService.delete(entity.getId());

        assertThat(xmEntityRepository.exists(parentEntity.getId())).isTrue();

        assertThat(xmEntityRepository.exists(entity.getId())).isFalse();

        assertThat(xmEntityRepository.exists(breakLink.getId())).isTrue();
        assertThat(xmEntityRepository.exists(cascadeDeleteLink.getId())).isFalse();

        assertThat(xmEntityRepository.exists(cascadeBreakSubLinks.getId())).isTrue();
        assertThat(xmEntityRepository.exists(cascadeDeleteSubLinks.getId())).isFalse();

        xmEntityService.delete(breakLink.getId());
        xmEntityService.delete(cascadeBreakSubLinks.getId());
        xmEntityService.delete(parentEntity.getId());
    }

    @Test
    @SneakyThrows
    public void testDeleteLinkDifferentLinkTypes() {

        XmEntity deletedEntity = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_DELETE_NEW_LINK"));
        XmEntity otherEntity = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TEST_DELETE_SEARCH_LINK"));
        XmEntity sharedEntity = xmEntityService.save(new XmEntity().name(" ").key(randomUUID()).typeKey("TARGET_ENTITY"));

        Link newLink = new Link();
        newLink.setTypeKey("newLink");
        newLink.setTarget(sharedEntity);
        deletedEntity.addTargets(newLink);

        Link searchLink = new Link();
        searchLink.setTypeKey("cascadeDeleteLinks");
        searchLink.setTarget(sharedEntity);
        otherEntity.addTargets(searchLink);

        xmEntityService.save(deletedEntity);
        xmEntityService.save(otherEntity);

        xmEntityService.delete(deletedEntity.getId());

        assertThat(xmEntityRepository.exists(otherEntity.getId())).isTrue();
        assertThat(xmEntityRepository.exists(sharedEntity.getId())).isTrue();
        assertThat(xmEntityRepository.exists(deletedEntity.getId())).isFalse();

        xmEntityService.delete(sharedEntity.getId());
        xmEntityService.delete(otherEntity.getId());
    }

}