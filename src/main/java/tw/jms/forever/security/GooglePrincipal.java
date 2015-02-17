package tw.jms.forever.security;

import java.security.Principal;

public class GooglePrincipal implements Principal {

	private String name;
	
	public GooglePrincipal(String name){
		this.name = name;
	}
	
	@Override
	public String getName() {		
		return name;
	}

}
