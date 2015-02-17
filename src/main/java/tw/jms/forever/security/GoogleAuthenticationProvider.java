package tw.jms.forever.security;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import tw.jms.forever.dao.UserRepository;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.model.Person.Emails;

@Component
public class GoogleAuthenticationProvider implements AuthenticationProvider {

	static Logger LOG = LogManager.getLogger(GoogleAuthenticationProvider.class
			.getName());

	@Autowired
	private Environment env;
	@Autowired
	private ForeverAuthorityGranter authorityGranter;

	@Autowired
	private UserRepository userRepository;

	@SuppressWarnings("unchecked")
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		if (!(authentication instanceof GoogleAuthentication)) {
			throw new AuthenticationServiceException(
					"authentication failed due to parameter not a instance of GoogleAuthentication");
		}
		try {
			List<Emails> emails = (List<Emails>) authentication.getPrincipal();
			for (Emails email : emails) {
				if (userRepository.findTopByEmail(email.getValue()) != null) {
					return new GoogleAuthentication(
							authentication.getPrincipal(),
							authentication.getCredentials(),
							authorityGranter.grant(new GooglePrincipal(email
									.getValue())));
				}
			}
		} catch (Exception e) {
			throw new AuthenticationServiceException("auth is not valid", e);
		}
		throw new UsernameNotFoundException("username is not valid");

	}

	@Override
	public boolean supports(Class<?> authentication) {
		if (authentication.equals(GoogleAuthentication.class)) {
			return true;
		}
		return false;
	}

}
