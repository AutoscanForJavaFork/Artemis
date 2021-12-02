package de.tum.in.www1.artemis.config.connector;

import static de.tum.in.www1.artemis.config.Constants.SPRING_PROFILE_JENKINS;

import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.offbytwo.jenkins.JenkinsServer;

@Configuration
@Profile(SPRING_PROFILE_JENKINS)
public class JenkinsServerConfiguration {

    @Value("${artemis.continuous-integration.user}")
    private String jenkinsUser;

    @Value("${artemis.continuous-integration.password}")
    private String jenkinsPassword;

    @Value("${artemis.continuous-integration.url}")
    private URL jenkinsServerUrl;

    @Bean
    public JenkinsServer jenkinsServer() throws URISyntaxException {
        return new JenkinsServer(jenkinsServerUrl.toURI(), jenkinsUser, jenkinsPassword);
    }

}
