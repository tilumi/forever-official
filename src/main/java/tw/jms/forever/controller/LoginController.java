package tw.jms.forever.controller;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Response;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import tw.jms.forever.security.GoogleAuthentication;
import tw.jms.forever.security.GoogleAuthenticationProvider;
import tw.jms.forever.util.EnvKeys;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

@Controller
public class LoginController {

	static Logger LOG = LogManager.getLogger(LoginController.class.getName());

	@Autowired
	private Environment env;

	@Autowired
	private AuthorizationCodeFlow authorizationFlow;

	@Autowired
	GoogleAuthenticationProvider googleAuthenticationProvider;

	private String host;
	private String port;

	@PostConstruct
	public void init() {
		this.host = env.getProperty(EnvKeys.FOREVER_SERVER_HOST);
		this.port = env.getProperty(EnvKeys.SERVER_PORT);
	}

	private String getRedirectUrl() {
		return "https://" + this.host + ":" + this.port + "/oauth2callback";
	}

	@RequestMapping("/login.html")
	public ModelAndView login(
			@RequestParam(value = "targetView", required = false, defaultValue = "/index.html") String targetView) {
		LOG.info(SecurityContextHolder.getContext().getAuthentication()
				.getClass().getName());
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (auth instanceof GoogleAuthentication && auth.isAuthenticated()) {
			return new ModelAndView("forward:index");
		}
		AuthorizationCodeRequestUrl url = authorizationFlow
				.newAuthorizationUrl();
		String urlString = url.setRedirectUri(getRedirectUrl())
				.setState(targetView).build();
		LOG.info("authUrl: " + urlString);
		return new ModelAndView("redirect:" + urlString);
	}

	@RequestMapping("/oauth2callback")
	public ModelAndView oauth2Callback(
			@RequestParam("code") String code,
			@RequestParam(value = "state", required = false, defaultValue = "/index.html") String targetView,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (code == null || code.isEmpty()) {
			throw new RuntimeException("code is not exist!");
		}
		TokenResponse tokenResponse = authorizationFlow.newTokenRequest(code)
				.setRedirectUri(getRedirectUrl()).execute();
		authorizationFlow.createAndStoreCredential(tokenResponse, "me");
		Credential cred = authorizationFlow.loadCredential("me");
		Plus plus = new Plus.Builder(new NetHttpTransport(),
				JacksonFactory.getDefaultInstance(), cred).build();
		Person me = plus.people().get("me").execute();
		Authentication auth = new GoogleAuthentication(me.getEmails(), cred);

		auth = googleAuthenticationProvider.authenticate(auth);
		SecurityContextHolder.getContext().setAuthentication(auth);
		return new ModelAndView("redirect:" + targetView);
	}

	@RequestMapping("/logout.html")
	public ModelAndView logout() throws IOException {
		GoogleAuthentication auth = (GoogleAuthentication) SecurityContextHolder
				.getContext().getAuthentication();		
		Credential cred = (Credential) auth.getCredentials();
		HttpResponse revokeResponse = new NetHttpTransport()
				.createRequestFactory()
				.buildGetRequest(
						new GenericUrl(
								String.format(
										"https://accounts.google.com/o/oauth2/revoke?token=%s",
										cred.getAccessToken()))).execute();
		if (revokeResponse.getStatusCode() == Response.SC_OK) {
			SecurityContextHolder.getContext().setAuthentication(null);
			return new ModelAndView("redirect:/index.html");
		} else {
			throw new RuntimeException("revoke google access token failed.");
		}
	}

}
