package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId)
    {
        if (parentId == null || StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByErrorMessage("添加参数品类错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0)
        {
            return ServerResponse.createBySuccess("添加品类成功");
        }

        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName)
    {
        if (categoryId == null || StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByErrorMessage("更新参数品类错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0)
        {
            return ServerResponse.createBySuccess("更新品类名字成功");
        }

        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId)
    {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList))
        {
            logger.info("未找到当前分类的子分类");
        }

        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse selectCategoryAndChildrenById(Integer categoryId)
    {
        if (categoryId != null)
        {
            Set<Category> categorySet = new HashSet<>();
            findChildCategory(categorySet, categoryId);

            List<Integer> categoryIdList = new ArrayList<>();
            for (Category categoryItem : categorySet)
            {
                categoryIdList.add(categoryItem.getId());
            }
            return ServerResponse.createBySuccess(categoryIdList);
        }

        return ServerResponse.createByError();
    }

    //这个递归函数损失了树形结构
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId)
    {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null)
        {
            categorySet.add(category);
        }//这里去重是因为，可能有个节点有不止一个父亲。。。

        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList)
        {
            findChildCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
