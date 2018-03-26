package revaligner.applicationconfiguration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration
  extends WebSecurityConfigurerAdapter
{
  @Autowired
  public void configureGlobalSecurity(AuthenticationManagerBuilder auth)
    throws Exception
  {
    InputStream in = getClass().getClassLoader().getResourceAsStream("SpecialUsers.properties");
    InputStreamReader isr = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(isr);
    String lineRead;
    while ((lineRead = br.readLine()) != null)
    {
      String[] splits = lineRead.split("\t");
      if (splits.length == 3) {
        auth.inMemoryAuthentication().withUser(splits[0]).password(splits[1]).roles(new String[] { splits[2] });
      }
    }
    br.close();
    isr.close();
    in.close();
    

    auth.userDetailsService(inMemoryUserDetailsManager());
  }
  
  protected void configure(HttpSecurity http)
    throws Exception
  {
    http.headers().frameOptions().sameOrigin();
    http.csrf().disable();
    http.authorizeRequests()
    	.antMatchers(new String[] { "/rac/login" }).permitAll()
		.antMatchers(new String[] { "/rac/entry" }).permitAll()
		.antMatchers(new String[] { "/rac/sessiontimesout" }).permitAll()
		.antMatchers(new String[] { "/rac/user/checkuser" }).permitAll()
		.antMatchers(new String[] { "/**" }).access("hasAnyRole('USER','ADMIN')")
		.and().formLogin().loginPage("/rac/login").usernameParameter("ssoId").passwordParameter("password")
		.and().exceptionHandling().accessDeniedPage("/Access_Denied");
  }
  
  public void configure(WebSecurity web)
    throws Exception
  {
    web.ignoring().antMatchers(new String[] { "/js/**" });
    web.ignoring().antMatchers(new String[] { "/css/**" });
    web.ignoring().antMatchers(new String[] { "/fonts/**" });
    web.ignoring().antMatchers(new String[] { "/images/**" });
    web.ignoring().antMatchers(new String[] { "/langs/**" });
    web.ignoring().antMatchers(new String[] { "/less/**" });
    web.ignoring().antMatchers(new String[] { "/scss/**" });
  }
  
  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager()
  {
    return new InMemoryUserDetailsManager();
  }
}
