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
	 * Registers MessageDispatcherServlet to handle SOAP requests at /createdNoc endpoint
	 * 
	 * @param context Application context
	 * @return Servlet registration bean
	 */
	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(context);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/createdNoc/*");
	}

	/**
	 * Creates WSDL definition for NOCAS SOAP service
	 * 
	 * @param nocasSchema XSD schema for WSDL generation
	 * @return WSDL definition bean
	 */
	@Bean(name = "nocas")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema nocasSchema) {
		DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
		wsdl.setPortTypeName("NocasPort");
		wsdl.setLocationUri("/createdNoc");
		wsdl.setTargetNamespace("http://upyog.org/noc");
		wsdl.setSchema(nocasSchema);
		return wsdl;
	}

	/**
	 * Loads XSD schema from classpath
	 * 
	 * @return XSD schema bean
	 */
	@Bean
	public XsdSchema nocasSchema() {
		return new SimpleXsdSchema(new ClassPathResource("nocas.xsd"));
	}

}