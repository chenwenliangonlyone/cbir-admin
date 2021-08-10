
package com.imt.service.dto;

import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

import com.imt.annotation.Query;

@Data
public class LocalStorageQueryCriteria{

    @Query(blurry = "name,suffix,type,createBy,size")
    private String blurry;

    @Query
    private String createBy;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}