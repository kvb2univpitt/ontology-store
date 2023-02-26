/*
 * Copyright (C) 2022 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.i2b2.ontologystore.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.delegate.RequestHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * Oct 10, 2022 7:47:57 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractWebService implements ServiceLifeCycle {

    private static final Log LOGGER = LogFactory.getLog(AbstractWebService.class);

    private static final String UNKNOWN_ERROR_MESSAGE = "Error message delivered from the remote server. You may wish to retry your last action";

    @Override
    public void startUp(ConfigurationContext context, AxisService service) {
        ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"}, false);
        appCtx.setClassLoader(service.getClassLoader());
        appCtx.refresh();
    }

    @Override
    public void shutDown(ConfigurationContext context, AxisService service) {
    }

    protected String unescapeXml(String xml) {
        return xml
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "\'")
                .replaceAll("&amp;", "&");
    }

    protected OMElement getNullRequestResponse() throws I2B2Exception {
        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, UNKNOWN_ERROR_MESSAGE);
        String ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);

        return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool, long waitTime) throws I2B2Exception {
        pool.shutdown();
        try {
            boolean isTerminated = (waitTime > 0)
                    ? pool.awaitTermination(waitTime, TimeUnit.MILLISECONDS)
                    : pool.awaitTermination(24, TimeUnit.HOURS);

            // force shutdown if the executor is not terminated
            if (!isTerminated) {
                pool.shutdownNow();
                if (!pool.awaitTermination(6000, TimeUnit.MILLISECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException exception) {
            pool.shutdownNow();

            LOGGER.error(exception.getMessage());
            throw new I2B2Exception("Thread error while running OntologyStore job.");
        }
    }

    protected OMElement execute(RequestHandler requestHandler, long waitTime) throws I2B2Exception {
        ExecutorRunnable runnable = new ExecutorRunnable(requestHandler);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(runnable);
        shutdownAndAwaitTermination(executorService, waitTime);

        String responseData = runnable.getOutput();
        if (responseData == null) {
            if (runnable.getException() != null) {
                LOGGER.error("runnable.jobException is " + runnable.getException().getMessage());
                LOGGER.info("waitTime is " + waitTime);

                ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, runnable.getException().getMessage());
                responseData = MessageFactory.convertToXMLString(responseMsgType);
            } else if (!runnable.isJobCompleted()) {
                String timeOuterror = "Remote server timed out \n"
                        + "Result waittime = " + waitTime
                        + " ms elapsed,\nPlease try again";
                LOGGER.error(timeOuterror);

                ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, timeOuterror);
                responseData = MessageFactory.convertToXMLString(responseMsgType);
            } else {
                LOGGER.error("ontologystore data response is null");
                LOGGER.info("waitTime is " + waitTime);
                ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, UNKNOWN_ERROR_MESSAGE);
                responseData = MessageFactory.convertToXMLString(responseMsgType);
            }
        }

        return MessageFactory.createResponseOMElementFromString(responseData);
    }

}
