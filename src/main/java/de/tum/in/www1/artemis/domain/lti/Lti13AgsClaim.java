package de.tum.in.www1.artemis.domain.lti;

import java.util.*;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * A wrapper class for a LTI 1.3 Assignment and Grading Services Claim.
 *
 * Currently we support the Score Publishing Service in order to transmit scores.
 */
public class Lti13AgsClaim {

    private List<String> scope;

    private String lineItem;

    /**
     * Returns an Ags-Claim representation if the provided idToken contains any.
     * @param idToken to be parsed
     * @return an Ags-Claim if one was present in idToken.
     */
    public static Optional<Lti13AgsClaim> from(OidcIdToken idToken) {
        JSONObject agsClaimJson = idToken.getClaim(Claims.AGS_CLAIM);
        if (agsClaimJson == null) {
            return Optional.empty();
        }

        Lti13AgsClaim agsClaim = new Lti13AgsClaim();
        JSONArray scopes = (JSONArray) agsClaimJson.get("scope");

        if (scopes == null) {
            return Optional.empty();
        }

        if (scopes.contains(Scope.SCORE)) {
            agsClaim.setScope(new LinkedList<>(Collections.singletonList(Scope.SCORE)));
        }

        agsClaim.setLineItem((String) agsClaimJson.get("lineitem"));

        return Optional.of(agsClaim);
    }

    public List<String> getScope() {
        return scope;
    }

    private void setScope(List<String> scope) {
        this.scope = scope;
    }

    public String getLineItem() {
        return lineItem;
    }

    private void setLineItem(String lineItem) {
        this.lineItem = lineItem;
    }

    public static class Scope {

        public static String SCORE = "https://purl.imsglobal.org/spec/lti-ags/scope/score";
    }
}
