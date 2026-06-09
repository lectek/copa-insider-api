package br.com.lectek.copainsider.application.core.auth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class OAuth2LinkService extends DefaultOAuth2UserService {

    private final SocialCustomerService socialCustomerService;

    public OAuth2LinkService(SocialCustomerService socialCustomerService) {
        this.socialCustomerService = socialCustomerService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attrs = oauth.getAttributes();

        String email         = extractEmail(provider, attrs);
        String name          = extractName(provider, attrs);
        String userId        = extractId(provider, attrs);
        String avatar        = extractAvatar(provider, attrs);
        boolean emailVerified= extractEmailVerified(provider, attrs);

        socialCustomerService.linkOrCreate(provider, userId, email, name, avatar, emailVerified);
        return oauth;
    }

    private String extractEmail(String provider, Map<String, Object> a) {
        Object v = a.get("email");
        return v != null ? v.toString() : null;
    }

    private boolean extractEmailVerified(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("email_verified");
            if (v instanceof Boolean b) return b;
            if (v != null) return Boolean.parseBoolean(v.toString());
        }
        return false;
    }

    private String extractName(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            for (String k : new String[]{"name", "given_name", "family_name"}) {
                Object v = a.get(k);
                if (v != null) return v.toString();
            }
            return "Cliente";
        }
        Object v = a.get("name");
        return v != null ? v.toString() : "Cliente";
    }

    private String extractId(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("sub");
            return v != null ? v.toString() : null;
        }
        Object v = a.get("id");
        return v != null ? v.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private String extractAvatar(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("picture");
            return v != null ? v.toString() : null;
        }
        Object pic = a.get("picture");
        if (pic instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) pic;
            Object data = m.get("data");
            if (data instanceof Map) {
                Object url = ((Map<String, Object>) data).get("url");
                if (url != null) return url.toString();
            }
        }
        return null;
    }
}
