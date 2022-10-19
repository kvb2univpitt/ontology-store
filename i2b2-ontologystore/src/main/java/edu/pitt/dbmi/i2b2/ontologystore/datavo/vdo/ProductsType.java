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
package edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * Oct 18, 2022 8:39:11 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "productsType", propOrder = {
    "products"
})
public class ProductsType {

    @XmlElement(name = "product")
    private List<ProductType> products;

    public ProductsType() {
    }

    public ProductsType(List<ProductType> products) {
        this.products = products;
    }

    public List<ProductType> getProducts() {
        return products;
    }

    public void setProducts(List<ProductType> products) {
        this.products = products;
    }

}
