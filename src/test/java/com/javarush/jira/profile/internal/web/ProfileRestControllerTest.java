package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.javarush.jira.common.util.JsonUtil.writeValue;
import static com.javarush.jira.login.internal.web.UserTestData.*;
import static com.javarush.jira.profile.internal.web.ProfileTestData.PROFILE_MATCHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String REST_URL = ProfileRestController.REST_URL;

    @Autowired
    ProfileMapper mapper;
    @Autowired
    private ProfileRepository repository;

    @Test
    void getUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getProfileAdminTest() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ADMIN_ID));
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void getProfileUserTest() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID));
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update() throws Exception {
        Profile profileBefore = repository.getOrCreate(USER_ID);
        ProfileTo profileTo = mapper.toTo(ProfileTestData.getUpdated(USER_ID));
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Profile dbProfileAfter= repository.getOrCreate(USER_ID);
        assertEquals(profileBefore.getLastLogin(), dbProfileAfter.getLastLogin(), "profile LastLogin must not be changed");
        Profile updated = ProfileTestData.getUpdated(USER_ID);
        PROFILE_MATCHER.assertMatch(dbProfileAfter, updated);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateOK() throws Exception {
        ProfileTo profileTo = ProfileTestData.getUpdatedTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateInvalid() throws Exception {
        ProfileTo profileTo = ProfileTestData.getInvalidTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void withUnknownNotification() throws Exception {
        ProfileTo profileTo = ProfileTestData.getWithUnknownNotificationTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
               .contentType(MediaType.APPLICATION_JSON)
               .content(writeValue(profileTo)))
               .andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }
    @Test
    @WithUserDetails(value = USER_MAIL)
    void withUnknownContact() throws Exception {
        ProfileTo profileTo = ProfileTestData.getWithUnknownContactTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void withContactHtmlUnsafe() throws Exception {
        ProfileTo profileTo = ProfileTestData.getWithContactHtmlUnsafeTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
               .contentType(MediaType.APPLICATION_JSON)
               .content(writeValue(profileTo)))
               .andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

}