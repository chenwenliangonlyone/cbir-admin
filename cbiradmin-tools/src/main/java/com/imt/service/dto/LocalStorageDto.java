
package com.imt.service.dto;

import lombok.Getter;
import lombok.Setter;
import com.imt.base.BaseDTO;
import java.io.Serializable;

@Getter
@Setter
public class LocalStorageDto extends BaseDTO implements Serializable {

    private Long id;

    private String realName;

    private String name;

    private String suffix;

    private String type;

    private String size;

    private String labelId;

    private String labelName;
}