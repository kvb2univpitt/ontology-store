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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * Oct 13, 2022 4:15:14 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class SpringInitService implements ServiceLifeCycle {

    @Override
    public void startUp(ConfigurationContext context, AxisService service) {
        ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"}, false);
        appCtx.setClassLoader(service.getClassLoader());
        appCtx.refresh();
    }

    @Override
    public void shutDown(ConfigurationContext context, AxisService service) {
    }

}
