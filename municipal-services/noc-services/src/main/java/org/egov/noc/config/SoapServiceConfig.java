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

	@Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/createdNoc/*");
    }

    @Bean(name = "nocas")  // ← WSDL name: /soap/nocas.wsdl
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema nocasSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("NocasPort");
        wsdl.setLocationUri("/createdNoc");
        wsdl.setTargetNamespace("http://egov.org/noc");  // ← Updated namespace
        wsdl.setSchema(nocasSchema);
        return wsdl;
    }

    @Bean
    public XsdSchema nocasSchema() {
        return new SimpleXsdSchema(new ClassPathResource("nocas.xsd"));
    }

    // Token-based authentication
//    @Bean
//    public Wss4jSecurityInterceptor securityInterceptor() {
//        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
//        interceptor.setSecurementActions("UsernameToken");
//        interceptor.setSecurementUsername("AAI");
//        interceptor.setSecurementPassword("AAI_PROVIDED_TOKEN");
//        interceptor.setValidationActions("UsernameToken");
//        interceptor.setValidationCallbackHandler(new SimpleAuthenticationHandler());
//        return interceptor;
//    }
//
//    @Override
//    public void addInterceptors(List<EndpointInterceptor> interceptors) {
//        interceptors.add(securityInterceptor());
//    }
}