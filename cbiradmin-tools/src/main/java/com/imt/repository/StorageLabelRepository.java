/*

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
package com.imt.repository;

import com.imt.domain.LocalStorage;
import com.imt.domain.StorageLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface StorageLabelRepository extends JpaRepository<StorageLabel, Long>, JpaSpecificationExecutor<StorageLabel> {
    /**
     * 根据名称查询
     * @param name 名称
     * @return /
     */
    StorageLabel findByLabelName(String name);

    /**
     * 根据Id删除
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据labelId查询是否有图片
     * @param ids /
     * @return /
     */
    @Query(value = "SELECT count(1) FROM tool_local_storage t WHERE t.label_id IN ?1", nativeQuery = true)
    int countImageByLabelId(Set<Long> ids);
}