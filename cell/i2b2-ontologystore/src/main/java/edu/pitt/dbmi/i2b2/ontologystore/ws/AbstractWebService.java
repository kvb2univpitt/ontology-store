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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 10, 2022 7:47:57 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractWebService {

    private static final Log LOGGER = LogFactory.getLog(AbstractWebService.class);

    private static final String UNKNOWN_ERROR_MESSAGE = "Error message delivered from the remote server. You may wish to retry your last action";

    protected OMElement getNullRequestResponse() throws I2B2Exception {
        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, UNKNOWN_ERROR_MESSAGE);
        String ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);

        return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
    }

    protected OMElement execute(RequestHandler requestHandler, long waitTime) throws I2B2Exception {
        ExecutorRunnable runnable = new ExecutorRunnable(requestHandler);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(runnable);
        executorService.shutdown();
        try {
            // timeout after 5 seconds
            if (waitTime > 0) {
                executorService.awaitTermination(waitTime, TimeUnit.MILLISECONDS);
            } else {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            }
        } catch (InterruptedException exception) {
            LOGGER.error(exception.getMessage());
            throw new I2B2Exception("Thread error while running Ontology job.");
        } finally {
            executorService.shutdownNow();
        }

        String responseData = runnable.getOutput();
        if (responseData == null) {
            if (runnable.getException() != null) {
                LOGGER.error("runnable.jobException is " + runnable.getException().getMessage());
                LOGGER.info("waitTime is " + waitTime);

                ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, UNKNOWN_ERROR_MESSAGE);
                responseData = MessageFactory.convertToXMLString(responseMsgType);
            } else if (!runnable.isJobCompleted()) {
                String timeOuterror = "Remote server timed out \n"
                        + "Result waittime = " + waitTime
                        + " ms elapsed,\nPlease try again";
                LOGGER.error(timeOuterror);

                ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, timeOuterror);
                responseData = MessageFactory.convertToXMLString(responseMsgType);
            } else {
                LOGGER.error("ontology data response is null");
                LOGGER.info("waitTime is " + waitTime);
                ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, UNKNOWN_ERROR_MESSAGE);
                responseData = MessageFactory.convertToXMLString(responseMsgType);
            }
        }

        return MessageFactory.createResponseOMElementFromString(responseData);
    }

//    protected OMElement execute(RequestHandler handler, long waitTime) throws I2B2Exception {
//        // do Ontology query processing inside thread, so that
//        // service could sends back message with timeout error.
//        String unknownErrorMessage = "Error message delivered from the remote server \n"
//                + "You may wish to retry your last action";
//
//        ExecutorRunnable er = new ExecutorRunnable(handler);
//
//        Thread t = new Thread(er);
//        String ontologyDataResponse = null;
//
//        synchronized (t) {
//            t.start();
//
//            try {
//                long startTime = System.currentTimeMillis();
//                long deltaTime = -1;
//                while ((er.isJobCompleted() == false)
//                        && (deltaTime < waitTime)) {
//                    if (waitTime > 0) {
//                        t.wait(waitTime - deltaTime);
//                        deltaTime = System.currentTimeMillis() - startTime;
//                    } else {
//                        t.wait();
//                    }
//                }
//
//                ontologyDataResponse = er.getOutput();
//
//                if (ontologyDataResponse == null) {
//                    if (er.getException() != null) {
//                        LOGGER.error("er.jobException is " + er.getException().getMessage());
//                        LOGGER.info("waitTime is " + waitTime);
//
//                        ResponseMessageType responseMsgType = MessageFactory
//                                .doBuildErrorResponse(null, unknownErrorMessage);
//                        ontologyDataResponse = MessageFactory
//                                .convertToXMLString(responseMsgType);
//
//                    } else if (er.isJobCompleted() == false) {
//                        // <result_waittime_ms>5000</result_waittime_ms>
//                        String timeOuterror = "Remote server timed out \n"
//                                + "Result waittime = " + waitTime
//                                + " ms elapsed,\nPlease try again";
//                        LOGGER.error(timeOuterror);
//
//                        LOGGER.debug("ontology waited " + deltaTime + "ms for "
//                                + er.getRequestHandler().getClass().getName());
//
//                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, timeOuterror);
//                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
//                    } else {
//                        LOGGER.error("ontology data response is null");
//                        LOGGER.info("waitTime is " + waitTime);
//                        LOGGER.debug("ontology waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName());
//                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrorMessage);
//                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
//                    }
//                }
//            } catch (InterruptedException e) {
//                LOGGER.error(e.getMessage());
//                throw new I2B2Exception(
//                        "Thread error while running Ontology job ");
//            } finally {
//                t.interrupt();
//                er = null;
//                t = null;
//            }
//        }
//
//        return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
//    }
}
