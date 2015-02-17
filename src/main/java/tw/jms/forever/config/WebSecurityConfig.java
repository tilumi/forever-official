package tw.jms.forever.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import tw.jms.forever.security.GoogleAuthenticationProvider;
import tw.jms.forever.util.EnvKeys;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.plus.PlusScopes;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;

	@Autowired
	private GoogleAuthenticationProvider googleAuthenticationProvider;

	@Bean
	public AuthorizationCodeFlow googleAuthorizationCodeFlow() {
		AuthorizationCodeFlow flow;
		String CLIENT_ID = env.getProperty(EnvKeys.GOOGLE_CLIENT_ID);
		String CLIENT_SECRET = env.getProperty(EnvKeys.GOOGLE_CLIENT_SECRET);
		final List<String> SCOPES = Arrays.asList(PlusScopes.USERINFO_PROFILE,
				PlusScopes.USERINFO_EMAIL);
		File dataStore = new File("googleOauth2DataStore");
		try {
			FileDataStoreFactory DATA_STORE_FACTORY = new FileDataStoreFactory(
					dataStore);
			flow = new GoogleAuthorizationCodeFlow.Builder(
					new NetHttpTransport(),
					JacksonFactory.getDefaultInstance(), CLIENT_ID,
					CLIENT_SECRET, SCOPES)
					.setDataStoreFactory(DATA_STORE_FACTORY)
					.setAccessType("offline").build();
			return flow;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/woogle/**")
				.access("hasRole('USER')").antMatchers("/logout.html")
				.access("hasRole('USER')").antMatchers("/login.html")
				.anonymous().antMatchers("/index.html").anonymous().and()
				.logout();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.authenticationProvider(googleAuthenticationProvider);
	}

}
