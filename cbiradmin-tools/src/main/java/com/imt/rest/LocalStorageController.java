package com.imt.rest;

import com.imt.domain.LocalStorage;
import com.imt.service.LocalStorageService;
import com.imt.service.dto.LocalStorageQueryCriteria;
import com.imt.utils.SecurityUtils;
import com.imt.utils.enums.DataScopeEnum;
import lombok.RequiredArgsConstructor;
import com.imt.annotation.Log;
import com.imt.exception.BadRequestException;
import com.imt.utils.FileUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@Api(tags = "工具：本地存储管理")
@RequestMapping("/api/localStorage")
public class LocalStorageController {

    private final LocalStorageService localStorageService;

    @ApiOperation("查询文件")
    @GetMapping
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> query(LocalStorageQueryCriteria criteria, Pageable pageable){
        //增加createBy条件 查询权限内文件
        String scopeType = SecurityUtils.getDataScopeType();
        if (DataScopeEnum.ALL.getValue() != scopeType){
            criteria.setCreateBy(SecurityUtils.getCurrentUsername());
        }
        return new ResponseEntity<>(localStorageService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('storage:list')")
    public void download(HttpServletResponse response, LocalStorageQueryCriteria criteria) throws IOException {
        //增加createBy条件 查询权限内文件
        String scopeType = SecurityUtils.getDataScopeType();
        if (DataScopeEnum.ALL.getValue() != scopeType){
            criteria.setCreateBy(SecurityUtils.getCurrentUsername());
        }
        localStorageService.download(localStorageService.queryAll(criteria), response);
    }

    @ApiOperation("上传文件")
    @PostMapping
    @PreAuthorize("@el.check('storage:add')")
    public ResponseEntity<Object> create(@RequestParam String name, @RequestParam("file") MultipartFile file){
        localStorageService.create(name,null,null, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/pictures")
    @ApiOperation("上传图片")
    public ResponseEntity<Object> upload(@RequestParam Long labelId, @RequestParam String labelName, @RequestParam("file") MultipartFile[] file){
        // 判断文件是否为图片
        ArrayList<LocalStorage> retList = new ArrayList<LocalStorage>();
        if (labelId == null || labelName == ""){
            throw new BadRequestException("请上传标签ID和标签名称");
        }
        if (file.length == 0) {
            throw new BadRequestException("上传图片不能为空");
        }
        for (MultipartFile multipartFile : file) {
            String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
            if(!FileUtil.IMAGE.equals(FileUtil.getFileType(suffix))){
                throw new BadRequestException("只能上传图片");
            }
            LocalStorage localStorage = localStorageService.create(null, labelId, labelName, multipartFile);
            retList.add(localStorage);
        }
        return new ResponseEntity<>(retList, HttpStatus.OK);
    }

    @Log("修改文件")
    @ApiOperation("修改文件")
    @PutMapping
    @PreAuthorize("@el.check('storage:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody LocalStorage resources){
        localStorageService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除文件")
    @DeleteMapping
    @ApiOperation("多选删除")
    public ResponseEntity<Object> delete(@RequestBody Long[] ids) {
        localStorageService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}