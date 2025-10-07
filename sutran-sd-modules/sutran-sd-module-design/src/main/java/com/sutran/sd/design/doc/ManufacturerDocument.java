package com.sutran.sd.design.doc; // 請根據您的專案結構修改此套件路徑

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *meilisearch model
 */
@Data
public class ManufacturerDocument implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long id;


    private String name;


    private List<String> tags;
}
