package tw.jms.forever.security;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class GoogleAuthentication extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 5157594500013239560L;

	public GoogleAuthentication(Object principal, Object credentials) {
		super(principal, credentials);
		// TODO Auto-generated constructor stub
	}

	public GoogleAuthentication(Object principal, Object credentials,
			List<GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
	}

}
