
package com.imt.service.dto;

import com.imt.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class StorageLabelDto extends BaseDTO implements Serializable {

    private Long id;
    private String labelName;
}