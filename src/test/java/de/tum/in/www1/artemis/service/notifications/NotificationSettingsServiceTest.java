package de.tum.in.www1.artemis.service.notifications;

import static de.tum.in.www1.artemis.domain.enumeration.NotificationType.*;
import static de.tum.in.www1.artemis.service.notifications.NotificationSettingsCommunicationChannel.EMAIL;
import static de.tum.in.www1.artemis.service.notifications.NotificationSettingsService.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.tum.in.www1.artemis.AbstractSpringIntegrationBambooBitbucketJiraTest;
import de.tum.in.www1.artemis.domain.NotificationSetting;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.domain.enumeration.NotificationType;
import de.tum.in.www1.artemis.domain.notification.GroupNotification;
import de.tum.in.www1.artemis.domain.notification.Notification;
import de.tum.in.www1.artemis.domain.notification.NotificationTitleTypeConstants;
import de.tum.in.www1.artemis.repository.NotificationSettingRepository;
import de.tum.in.www1.artemis.security.SecurityUtils;

public class NotificationSettingsServiceTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    private NotificationSettingsService notificationSettingsService;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    private Notification notification;

    private User student1;

    private NotificationSetting completeNotificationSettingA;

    private NotificationSetting[] unsavedNotificationSettings;

    private NotificationSetting[] savedNotificationSettings;

    /**
     * Prepares the needed values and objects for testing
     */
    @BeforeEach
    public void setUp() {
        SecurityUtils.setAuthorizationObject();

        List<User> users = database.addUsers(1, 0, 0, 0);
        student1 = users.get(0);

        NotificationSetting unsavedNotificationSettingA = new NotificationSetting(false, true, NOTIFICATION__EXERCISE_NOTIFICATION__EXERCISE_OPEN_FOR_PRACTICE);
        NotificationSetting unsavedNotificationSettingB = new NotificationSetting(true, true, NOTIFICATION__LECTURE_NOTIFICATION__ATTACHMENT_CHANGES);
        NotificationSetting unsavedNotificationSettingC = new NotificationSetting(false, false, NOTIFICATION__INSTRUCTOR_NOTIFICATION__COURSE_AND_EXAM_ARCHIVING_STARTED);
        unsavedNotificationSettings = new NotificationSetting[] { unsavedNotificationSettingA, unsavedNotificationSettingB, unsavedNotificationSettingC };

        completeNotificationSettingA = new NotificationSetting(student1, false, true, NOTIFICATION__EXERCISE_NOTIFICATION__EXERCISE_OPEN_FOR_PRACTICE);
        NotificationSetting completeNotificationSettingB = new NotificationSetting(student1, true, true, NOTIFICATION__LECTURE_NOTIFICATION__ATTACHMENT_CHANGES);
        NotificationSetting completeNotificationSettingC = new NotificationSetting(student1, false, false,
                NOTIFICATION__INSTRUCTOR_NOTIFICATION__COURSE_AND_EXAM_ARCHIVING_STARTED);
        savedNotificationSettings = new NotificationSetting[] { completeNotificationSettingA, completeNotificationSettingB, completeNotificationSettingC };

        notificationSettingRepository.saveAll(Arrays.stream(savedNotificationSettings).toList());

        notification = new GroupNotification();
    }

    @AfterEach
    public void resetDatabase() {
        database.resetDatabase();
    }

    /**
     * Tests the method setCurrentUser
     * Each provided notification setting should have the same user afterwards
     */
    @Test
    public void testSetCurrentUser() {
        NotificationSetting[] tmpNotificationSettings = Arrays.copyOf(unsavedNotificationSettings, unsavedNotificationSettings.length);

        notificationSettingsService.setCurrentUser(unsavedNotificationSettings, student1);

        for (NotificationSetting tmpSetting : tmpNotificationSettings) {
            assertThat(tmpSetting.getUser()).as("User was correctly set for NotificationSetting").isEqualTo(student1);
        }
    }

    /**
     * Tests the method findDeactivatedNotificationTypes
     * This test also tests the private methods convertNotificationSettingsToNotificationTypesWithActivationStatus
     * & convertNotificationSettingsToNotificationTypesWithActivationStatus
     */
    @Test
    public void testFindDeactivatedNotificationTypes() {
        NotificationSetting[] tmpNotificationSettingsArray = Arrays.copyOf(savedNotificationSettings, savedNotificationSettings.length);
        Set<NotificationSetting> tmpNotificationSettingsSet = new HashSet<>(Arrays.asList(tmpNotificationSettingsArray));
        Set<NotificationType> resultingTypeSet = notificationSettingsService.findDeactivatedNotificationTypes(NotificationSettingsCommunicationChannel.WEBAPP,
                tmpNotificationSettingsSet);
        // SettingA : exercise-open-for-practice -> [EXERCISE_PRACTICE] : webapp deactivated
        // SettingB : attachment-changes -> [ATTACHMENT_CHANGE] : webapp activated <- not part of set
        // SettingC : course-and-exam-archiving-started -> [EXAM_ARCHIVE_STARTED, COURSE_ARCHIVE_STARTED] : webapp deactivated
        assertThat(resultingTypeSet).as("The resulting type set should contain all 3 corresponding types").hasSize(3);
        assertThat(resultingTypeSet).as("The type for EXERCISE_PRACTICE should be returned").contains(EXERCISE_PRACTICE);
        assertThat(resultingTypeSet).as("The type for EXAM_ARCHIVE_STARTED should be returned").contains(EXAM_ARCHIVE_STARTED);
        assertThat(resultingTypeSet).as("The type for COURSE_ARCHIVE_STARTED should be returned").contains(COURSE_ARCHIVE_STARTED);
        assertThat(resultingTypeSet).as("The type for COURSE_ARCHIVE_STARTED should be returned").contains(COURSE_ARCHIVE_STARTED);
        assertThat(resultingTypeSet).as("The type for ATTACHMENT_CHANGE should be returned").doesNotContain(ATTACHMENT_CHANGE);
    }

    /**
     * Tests the method checkIfNotificationEmailIsAllowedBySettingsForGivenUser
     * Checks if the given user should receive an email based on the specific notification (type) or not based on the user's notification settings
     */
    @Test
    public void testCheckIfNotificationEmailIsAllowedBySettingsForGivenUser() {
        notification.setTitle(NotificationTitleTypeConstants.findCorrespondingNotificationTitle(ATTACHMENT_CHANGE));
        assertThat(notificationSettingsService.checkIfNotificationOrEmailIsAllowedBySettingsForGivenUser(notification, student1, EMAIL))
                .as("Emails with type ATTACHMENT_CHANGE should be allowed for the given user").isTrue();

        notification.setTitle(NotificationTitleTypeConstants.findCorrespondingNotificationTitle(EXERCISE_PRACTICE));
        assertThat(notificationSettingsService.checkIfNotificationOrEmailIsAllowedBySettingsForGivenUser(notification, student1, EMAIL))
                .as("Emails with type EXERCISE_PRACTICE should be allowed for the given user").isTrue();

        notification.setTitle(NotificationTitleTypeConstants.findCorrespondingNotificationTitle(EXAM_ARCHIVE_STARTED));
        assertThat(notificationSettingsService.checkIfNotificationOrEmailIsAllowedBySettingsForGivenUser(notification, student1, EMAIL))
                .as("Emails with type EXAM_ARCHIVE_STARTED should not be allowed for the given user").isFalse();
    }

    /**
     * Tests the method checkLoadedNotificationSettingsForCorrectness with an empty input
     */
    @Test
    public void testCheckLoadedNotificationSettingsForCorrectness_empty() {
        Set<NotificationSetting> testSet = new HashSet<>();
        testSet = notificationSettingsService.checkLoadedNotificationSettingsForCorrectness(testSet);
        assertThat(testSet).as("The default notification settings should be returned").isEqualTo(DEFAULT_NOTIFICATION_SETTINGS);
    }

    /**
     * Tests the method checkLoadedNotificationSettingsForCorrectness with an incomplete input
     */
    @Test
    public void testCheckLoadedNotificationSettingsForCorrectness_incomplete() {
        Set<NotificationSetting> testSet = new HashSet<>();
        testSet.add(completeNotificationSettingA);
        testSet = notificationSettingsService.checkLoadedNotificationSettingsForCorrectness(testSet);
        assertThat(testSet.size()).as("The number of loaded Settings should be equals to the number of default settings").isEqualTo(DEFAULT_NOTIFICATION_SETTINGS.size());
        assertThat(testSet).as("The loaded settings should contain the set of test settings").contains(completeNotificationSettingA);
    }

    /**
     * Tests the method checkLoadedNotificationSettingsForCorrectness with a correct input
     */
    @Test
    public void testCheckLoadedNotificationSettingsForCorrectness_correct() {
        Set<NotificationSetting> testSet = new HashSet<>(DEFAULT_NOTIFICATION_SETTINGS);
        testSet = notificationSettingsService.checkLoadedNotificationSettingsForCorrectness(testSet);
        assertThat(testSet.size()).as("The number of loaded Settings should be equals to the number of default settings")
                .isEqualTo(NotificationSettingsService.DEFAULT_NOTIFICATION_SETTINGS.size());
    }

    /**
     * Tests the method checkLoadedNotificationSettingsForCorrectness with an outdated input
     */
    @Test
    public void testCheckLoadedNotificationSettingsForCorrectness_outdated() {
        NotificationSetting outdatedSetting = new NotificationSetting(student1, false, true, "Outdated Settings ID");
        notificationSettingRepository.save(outdatedSetting);

        Set<NotificationSetting> outdatedSet = notificationSettingRepository.findAllNotificationSettingsForRecipientWithId(student1.getId());

        assertThat(outdatedSet.size()).as("Prior to checking the settings for correctness the outdated additional setting should be present")
                .isNotEqualTo(NotificationSettingsService.DEFAULT_NOTIFICATION_SETTINGS.size());

        Set<NotificationSetting> resultingSet = notificationSettingsService.checkLoadedNotificationSettingsForCorrectness(outdatedSet);

        assertThat(resultingSet.size()).as("The number of loaded Settings should be equals to the number of default settings")
                .isEqualTo(NotificationSettingsService.DEFAULT_NOTIFICATION_SETTINGS.size());
        assertThat(resultingSet).as("The outdated setting should have been removed").doesNotContain(outdatedSetting);
    }
}
