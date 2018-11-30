package revaligner.controllers;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/user"})
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class UserController
{
  private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
  
  @Autowired
  public UserController(InMemoryUserDetailsManager inMemoryUserDetailsManager)
  {
    this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
  }
  
  @RequestMapping(value={"/checkuser"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void checkUserLogin(HttpServletResponse response, @RequestBody String arr)
    throws Exception
  {
    System.out.println("checking login credentials....");
    try
    {
      String decodedString = URLDecoder.decode(arr, "UTF-8");
      JSONObject json = new JSONObject(decodedString);
      
      String username = json.getString("username");
      String password = json.getString("password");
      
      Hashtable<String, String> environment = new Hashtable<String, String>();
      String ldapURL = "ldap://nysv-vmdc1.tpnyc.local";
      String domain = "tpnyc.local";
      //String base = "dc=tpnyc,dc=local";
      
      environment.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
      environment.put("java.naming.provider.url", ldapURL);
      environment.put("java.naming.security.authentication", "simple");
      environment.put("java.naming.security.principal", username + "@" + domain);
      environment.put("java.naming.security.credentials", password);
      try
      {
        @SuppressWarnings("unused")
		DirContext authContext = new InitialDirContext(environment);
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        if ((username.equals("nyammine")) || (username.equals("mxiang")) || (username.equals("njohnson")) || (username.equals("cfarah")) || (username.equals("ehom")) || (username.equals("bverbraak"))) {
          grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
          //grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
          grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        UserDetails user = new User(username, password, grantedAuthorityList);
        if (this.inMemoryUserDetailsManager.userExists(username))
        {
          System.out.println("exist");
          this.inMemoryUserDetailsManager.deleteUser(username);
        }
        this.inMemoryUserDetailsManager.createUser(user);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, grantedAuthorityList);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        System.out.println("user \"" + username + "\" login successfully.");
      }
      catch (AuthenticationException e)
      {
        System.out.println("invalid user login credentials");
      }
      catch (NamingException e)
      {
        System.out.println("failed to check user login credentials");
      }
    }
    catch (Exception e)
    {
      System.out.println("failed to check user login credentials");
      
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
}
