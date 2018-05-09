package gov.uk.ons.registers.microservice.config

import org.springframework.context.annotation.{Configuration, Profile}
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@Profile(Array("ca"))
class CASecurityConfiguration extends WebSecurityConfigurerAdapter {

  @throws[Exception]
  override def configure(web: WebSecurity): Unit = {
    web.ignoring.antMatchers("/resources/**")
  }

  @throws[Exception]
  override protected def configure(http: HttpSecurity): Unit = {
    super.configure(http)
    http.authorizeRequests.antMatchers("/message*")
      .hasRole("user")
      .anyRequest.permitAll
  }

}
