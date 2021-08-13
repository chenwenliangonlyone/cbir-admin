
package com.imt.service.dto;

import com.imt.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class StorageLabelQueryCriteria {

    @Query(blurry = "labelName,createBy")
    private String blurry;

    @Query
    private String createBy;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}