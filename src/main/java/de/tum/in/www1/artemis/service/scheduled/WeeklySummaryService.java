package de.tum.in.www1.artemis.service.scheduled;

import static de.tum.in.www1.artemis.service.notifications.NotificationSettingsService.NOTIFICATION__WEEKLY_SUMMARY_WEEKLY_SUMMARY_BASIC;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.NotificationSetting;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.repository.NotificationSettingRepository;
import de.tum.in.www1.artemis.repository.UserRepository;
import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.CourseService;
import de.tum.in.www1.artemis.service.MailService;
import tech.jhipster.config.JHipsterConstants;

@Service
@Profile("scheduling")
public class WeeklySummaryService {

    private final Logger log = LoggerFactory.getLogger(NotificationScheduleService.class);

    private final Environment environment;

    private final MailService mailService;

    private final UserRepository userRepository;

    private final NotificationSettingRepository notificationSettingRepository;

    private final CourseService courseService;

    private final TaskScheduler scheduler;

    private ZonedDateTime oneWeekAgo;

    public WeeklySummaryService(Environment environment, MailService mailService, UserRepository userRepository, NotificationSettingRepository notificationSettingRepository,
            CourseService courseService, TaskScheduler scheduler) {
        this.environment = environment;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.courseService = courseService;
        this.scheduler = scheduler;
    }

    /**
     * Prepare weekly summaries scheduling after server start up
     */
    @PostConstruct
    public void scheduleWeeklySummariesOnStartUp() {
        try {
            Collection<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
            if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)) {
                // only execute this on production server, i.e. when the prod profile is active
                // NOTE: if you want to test this locally, please comment it out, but do not commit the changes
                return;
            }
            SecurityUtils.setAuthorizationObject();

            // i.e. next Friday
            LocalDateTime nextWeeklySummaryDate = ZonedDateTime.now().toLocalDateTime().with(DayOfWeek.FRIDAY);
            // For local testing :
            // nextWeeklySummaryDate = ZonedDateTime.now().plusMinutes(5);

            ZoneOffset zoneOffset = ZonedDateTime.now().getZone().getRules().getOffset(Instant.now());

            scheduler.scheduleAtFixedRate(scheduleWeeklySummaries(), nextWeeklySummaryDate.toInstant(zoneOffset), Duration.ofDays(7));

            log.info("Scheduled weekly summaries on start up.");
        }
        catch (Exception exception) {
            log.error("Failed to start WeeklySummaryService", exception);
        }
    }

    /**
     * Begin the process of weekly summaries
     * i.e. find all active Artemis users that have weekly summaries enabled in their notification settings
     * and initiate the creation of weekly summary email for each found user.
     */
    @Async
    Runnable scheduleWeeklySummaries() {
        return () -> {
            checkSecurityUtils();
            oneWeekAgo = ZonedDateTime.now().minusWeeks(1);
            // find all Artemis users // Could be improved by getting only still active users
            Set<User> allUsers = userRepository.getAll();
            // filter out users that do not want to receive weekly summaries
            Set<User> filteredUsers = allUsers.stream().filter(this::checkIfWeeklySummaryIsAllowedByNotificationSettingsForGivenUser).collect(Collectors.toSet());
            if (!allUsers.isEmpty()) {
                filteredUsers.forEach(this::prepareWeeklySummaryForUser);
            }
        };
    }

    /**
     * Checks and sets the needed authentication
     */
    private void checkSecurityUtils() {
        if (!SecurityUtils.isAuthenticated()) {
            SecurityUtils.setAuthorizationObject();
        }
    }

    /**
     * Check if this user should receive weekly summary emails
     * @param user who is checked
     * @return true if user has weekly summaries enabled else false
     */
    private boolean checkIfWeeklySummaryIsAllowedByNotificationSettingsForGivenUser(User user) {
        Set<NotificationSetting> notificationSettings = notificationSettingRepository.findAllNotificationSettingsForRecipientWithId(user.getId());
        NotificationSetting weeklySummarySetting = notificationSettings.stream().filter(setting -> setting.getSettingId().equals(NOTIFICATION__WEEKLY_SUMMARY_WEEKLY_SUMMARY_BASIC))
                .findFirst().orElse(null);
        if (weeklySummarySetting == null)
            return false;
        return weeklySummarySetting.isEmail();
    }

    /**
     * Prepares all needed information to create the weekly summary email for one user
     * and calls the MailService
     * @param user for whom the weekly summary email should be prepared
     */
    private void prepareWeeklySummaryForUser(User user) {
        // Get all courses with exercises, lectures and exams (filtered for given user)
        List<Course> courses = courseService.findAllActiveWithExercisesAndLecturesAndExamsForUser(user);

        // Get released exercises of the last week (which are not yet over, i.e. working due date is not passed yet)
        Set<Exercise> newExercisesOfThisWeek = getAllExercisesOfThisWeek(courses);

        // More elements that should be displayed in weekly summaries can be extracted here
        // Currently only exercises are used for weekly summaries

        mailService.sendWeeklySummaryEmail(user, newExercisesOfThisWeek);
    }

    /**
     * @param courses which exercises will be extracted
     * @return all still active exercises of this week based on the students courses
     */
    private Set<Exercise> getAllExercisesOfThisWeek(List<Course> courses) {
        Set<Exercise> newExercisesOfThisWeek = new HashSet<>();
        courses.forEach(course -> newExercisesOfThisWeek
                .addAll(course.getExercises().stream().filter(exercise -> exercise.getReleaseDate().isAfter(oneWeekAgo) && !exercise.isEnded()).collect(Collectors.toSet())));
        return newExercisesOfThisWeek;
    }
}
