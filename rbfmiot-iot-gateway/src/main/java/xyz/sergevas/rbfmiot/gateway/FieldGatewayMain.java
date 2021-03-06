package xyz.sergevas.rbfmiot.gateway;

import static xyz.sergevas.rbfmiot.gateway.IConstants.DIRECT_FIELD_GATEWAY_INIT_RECEIVE_ENDPOINT;
import static xyz.sergevas.rbfmiot.gateway.IConstants.FIELD_GATEWAY_STATUS_ON;
import static xyz.sergevas.rbfmiot.gateway.IConstants.HONO_MESSAGE_HANDLER;
import static xyz.sergevas.rbfmiot.gateway.IConstants.LOG;
import static xyz.sergevas.rbfmiot.gateway.IConstants.TRANSFORM_UTILS;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;

import xyz.sergevas.rbfmiot.gateway.service.HonoMessageHandler;
import xyz.sergevas.rbfmiot.gateway.service.TransformUtils;
import static org.apache.camel.component.amqp.AMQPConnectionDetails.discoverAMQP;

public class FieldGatewayMain {
	
	private static final String CLASS_NAME = FieldGatewayMain.class.getName();

    public static void main(String... args) throws Exception {
    	LOG.info("{} Field Gateway initialisation start...", CLASS_NAME);
        Main main = new Main();
        main.bind("properties", new PropertiesComponent("classpath:field-gateway.properties"));
        main.bind("amqpConnection", discoverAMQP(main.getCamelContexts().get(0)));
        main.bind(TRANSFORM_UTILS, TransformUtils.class);
        main.bind(HONO_MESSAGE_HANDLER, HonoMessageHandler.class);
        main.addRouteBuilder(new FieldGatewayRoutes());
        main.addMainListener(new FieldGatewayInitManagement());
        LOG.info("{} Field Gateway initialisation complete...", CLASS_NAME);
        try {
        	LOG.info("{} Run Field Gateway ...", CLASS_NAME);
        	main.run(args);
        } catch (Exception e) {
        	LOG.error("{} Unable to run Field Gateway!", CLASS_NAME, e);
        }
    }
    
public static class FieldGatewayInitManagement extends MainListenerSupport {
    	
    	@Override
    	public void afterStart(MainSupport main) {
    		LOG.debug("{}.afterStart() start...", CLASS_NAME);
    		ProducerTemplate producerTemplate = null;
    		try {
				producerTemplate = main.getCamelTemplate();
				producerTemplate.sendBody("direct:" + DIRECT_FIELD_GATEWAY_INIT_RECEIVE_ENDPOINT, FIELD_GATEWAY_STATUS_ON);
			} catch (Exception e) {
				String errMsg = "Unable to init Field Gateway"; 
				LOG.error(errMsg, e);
				throw new RuntimeException(errMsg, e);
			}
    		LOG.debug("{}.afterStart() complete...", CLASS_NAME);
    	}
    	
    	@Override
    	public void afterStop(MainSupport main) {
    		LOG.debug("{}.afterStop() start...", CLASS_NAME);
    		LOG.debug("{}.afterStop() complete...", CLASS_NAME);
        }
    }
}
