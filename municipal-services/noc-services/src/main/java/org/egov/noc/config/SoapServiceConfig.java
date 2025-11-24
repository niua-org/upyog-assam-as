package org.egov.noc.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
//import org.springframework.ws.server.EndpointInterceptor;
//import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
@ComponentScan(basePackages = "org.egov.noc")
public class SoapServiceConfig extends WsConfigurerAdapter {

	/**
	 * Registers the MessageDispatcherServlet to handle SOAP requests.
	 *
	 * @param context the application context
	 * @return servlet registration bean for SOAP endpoint
	 */
	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(context);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/createdNoc/*");
	}

	/**
	 * Creates the WSDL definition for the NOCAS SOAP service.
	 *
	 * @param nocasSchema the XSD schema used for WSDL generation
	 * @return WSDL 1.1 definition bean
	 */
	@Bean(name = "nocas")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema nocasSchema) {
		DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
		wsdl.setPortTypeName("NocasPort");
		wsdl.setLocationUri("/createdNoc");
		wsdl.setTargetNamespace("http://egov.org/noc");
		wsdl.setSchema(nocasSchema);
		return wsdl;
	}

	/**
	 * Loads the XSD schema for the NOCAS SOAP service.
	 *
	 * @return XSD schema bean
	 */
	@Bean
	public XsdSchema nocasSchema() {
		return new SimpleXsdSchema(new ClassPathResource("nocas.xsd"));
	}

}