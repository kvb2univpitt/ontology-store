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

import edu.pitt.dbmi.i2b2.ontologystore.delegate.RequestHandler;

/**
 *
 * Oct 10, 2022 8:19:18 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class ExecutorRunnable implements Runnable {

    private Exception exception;
    private String input;
    private String output;
    private boolean jobCompleted;

    private final RequestHandler requestHandler;

    public ExecutorRunnable(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        try {
            this.output = requestHandler.execute();
            this.jobCompleted = true;
        } catch (Exception ex) {
            this.exception = ex;
        }
    }

    public Exception getException() {
        return exception;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public boolean isJobCompleted() {
        return jobCompleted;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

}
