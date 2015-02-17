package tw.jms.forever.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class ForeverAuthorityGranter{

	public List<GrantedAuthority> grant(Principal principal) {
		List<GrantedAuthority>  result = new ArrayList<GrantedAuthority> ();
		if(principal.getName().equals("tilumi0@gmail.com")){
			result.add(new SimpleGrantedAuthority("USER"));
		}
		return result;
	}

}
