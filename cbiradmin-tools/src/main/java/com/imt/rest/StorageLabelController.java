package com.imt.rest;

import com.imt.domain.StorageLabel;
import com.imt.service.StorageLabelService;
import com.imt.service.dto.StorageLabelQueryCriteria;
import com.imt.utils.SecurityUtils;
import com.imt.utils.enums.DataScopeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import com.imt.annotation.Log;
import com.imt.exception.BadRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Api(tags = "工具：标签管理")
@RequestMapping("/api/label")
public class StorageLabelController {

    private final StorageLabelService storageLabelService;
    private static final String ENTITY_NAME = "storageLabel";

    @ApiOperation("导出标签数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('label:list')")
    public void download(HttpServletResponse response, StorageLabelQueryCriteria criteria) throws IOException {
        //增加createBy条件 查询权限内文件
        String scopeType = SecurityUtils.getDataScopeType();
        if (DataScopeEnum.ALL.getValue() != scopeType){
            criteria.setCreateBy(SecurityUtils.getCurrentUsername());
        }
        storageLabelService.download(storageLabelService.queryAll(criteria), response);
    }

    @ApiOperation("查询标签")
    @GetMapping
    @PreAuthorize("@el.check('label:list')")
    public ResponseEntity<Object> query(StorageLabelQueryCriteria criteria, Pageable pageable){
        //增加createBy条件 查询权限内文件
        String scopeType = SecurityUtils.getDataScopeType();
        if (DataScopeEnum.ALL.getValue() != scopeType){
            criteria.setCreateBy(SecurityUtils.getCurrentUsername());
        }
        return new ResponseEntity<>(storageLabelService.queryAll(criteria, pageable),HttpStatus.OK);
    }

    @Log("新增标签")
    @ApiOperation("新增标签")
    @PostMapping
    @PreAuthorize("@el.check('label:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody StorageLabel resources){
//        if (resources.getId() != null) {
//            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
//        }
        //新增标签 并创建对应文件夹
        storageLabelService.create(resources);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改标签")
    @ApiOperation("修改标签")
    @PutMapping
    @PreAuthorize("@el.check('label:edit')")
    public ResponseEntity<Object> update(@Validated(StorageLabel.Update.class) @RequestBody StorageLabel resources){
        storageLabelService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //删除要删除对应的文件夹和包含的文件
    @Log("删除标签")
    @ApiOperation("删除标签")
    @DeleteMapping
    @PreAuthorize("@el.check('label:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        // 验证是否含有图片
        // 如有 删除失败，请先删除属于选定标签的所有图片
        storageLabelService.verification(ids);
        // 删除标签 并删除对应文件夹
        storageLabelService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}