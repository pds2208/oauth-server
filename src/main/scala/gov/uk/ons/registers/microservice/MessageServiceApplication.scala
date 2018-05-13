package gov.uk.ons.registers.microservice

import javax.net.ssl.SSLSession
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

object MessageServiceApplication {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[MessageServiceApplication], args: _*)
  }
}

@SpringBootApplication
class MessageServiceApplication {

  /** For localhost testing.
    *
    * This code is needed together with a certificate store containing
    * a certificate for localhost.
    *
    * Use the InstallCert application to generate a valid jssecacerts file.
    * Then copy the file to a location of your choice and add the following
    * command line option:
    *
    * -Djavax.net.ssl.trustStore=<location of your choice>jssecacerts
    *
    * */
  javax.net.ssl.HttpsURLConnection
    .setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

      override def verify(hostname: String, sslSession: SSLSession): Boolean = {
        if (hostname == "localhost") {
          return true
        }
        false
      }
    })
}
