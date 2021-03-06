/*

 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.imt.service.impl;

import com.imt.config.FileProperties;
import com.imt.domain.LocalStorage;
import com.imt.domain.StorageLabel;
import com.imt.exception.BadRequestException;
import com.imt.exception.EntityExistException;
import com.imt.repository.LocalStorageRepository;
import com.imt.repository.StorageLabelRepository;
import com.imt.service.StorageLabelService;
import com.imt.service.dto.StorageLabelDto;
import com.imt.service.dto.StorageLabelQueryCriteria;
import com.imt.service.mapstruct.StorageLabelMapper;
import com.imt.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "label")
public class StorageLabelServiceImpl implements StorageLabelService {

    private final StorageLabelRepository storageLabelRepository;
    private final LocalStorageRepository localStorageRepository;
    private final StorageLabelMapper storageLabelMapper;
    private final FileProperties properties;

    @Override
    public Map<String,Object> queryAll(StorageLabelQueryCriteria criteria, Pageable pageable) {
        Page<StorageLabel> page = storageLabelRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(storageLabelMapper::toDto).getContent(),page.getTotalElements());
    }

    @Override
    public List<StorageLabelDto> queryAll(StorageLabelQueryCriteria criteria) {
        List<StorageLabel> list = storageLabelRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder));
        return storageLabelMapper.toDto(list);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public StorageLabelDto findById(Long id) {
        StorageLabel label = storageLabelRepository.findById(id).orElseGet(StorageLabel::new);
        ValidationUtil.isNull(label.getId(),"Label","labelId",id);
        return storageLabelMapper.toDto(label);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(StorageLabel resources) {
        StorageLabel storageLabel = storageLabelRepository.findByLabelName(resources.getLabelName());
        if(storageLabel != null){
            throw new BadRequestException("???????????? [ " + resources.getLabelName() + " ] ?????????");
        }
        storageLabelRepository.save(resources);
        // ???????????????????????????
        try {
            String path = properties.getPath().getPath() + resources.getId();
            // getCanonicalFile ????????????
            File dest = new File(path);// ????????????
            dest.mkdirs();
        } catch (Exception e) {
            throw new BadRequestException("???????????????????????????");
        }
    }

    @Override
    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public void update(StorageLabel resources) {
        StorageLabel label = storageLabelRepository.findById(resources.getId()).orElseGet(StorageLabel::new);
        StorageLabel old = storageLabelRepository.findByLabelName(resources.getLabelName());
        if(old != null && !old.getId().equals(resources.getId())){
            throw new EntityExistException(StorageLabel.class,"labelName",resources.getLabelName());
        }
        ValidationUtil.isNull( label.getId(),"Label","labelId",resources.getId());
        resources.setId(label.getId());
        // ??????????????????????????????????????????
        List<LocalStorage> localStoragelist = localStorageRepository.findByLabelId(label.getId());
        for (LocalStorage localStorage : localStoragelist) {
            localStorage.setLabelName(resources.getLabelName());
            localStorageRepository.save(localStorage);
        }
        storageLabelRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        storageLabelRepository.deleteAllByIdIn(ids);
        // ???????????????????????????
        try {
            for (Long id : ids) {
                String path = properties.getPath().getPath() + id;
                File dest = new File(path);
                dest.delete();
            }
        }catch (Exception e){
            throw new BadRequestException("???????????????????????????");
        }
    }

    @Override
    public void download(List<StorageLabelDto> storageLabelDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (StorageLabelDto storageLabelDTO : storageLabelDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("??????ID", storageLabelDTO.getId());
            map.put("????????????", storageLabelDTO.getLabelName());
            map.put("?????????", storageLabelDTO.getCreateBy());
            map.put("????????????", storageLabelDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        if(storageLabelRepository.countImageByLabelId(ids) > 0){
            throw new BadRequestException("????????????????????????????????????????????????????????????");
        }
    }
}