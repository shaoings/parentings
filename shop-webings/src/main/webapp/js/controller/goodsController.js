 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,brandService,uploadService,itemCatService,typeTemplateService ){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//保存
	$scope.add=function () {
		//提取kindeditor编辑器的内容
		$scope.entity.goodsDesc.introduction = editor.html();

		goodsService.add($scope.entity).success(
			function (response) {
				if(response.success){
					alert('保存成功');
					$scope.entity = {};
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
            }
		);
    }

    //查询品牌
    $scope.queryBrand = function () {
        brandService.findAll().success(
        	function(response) {
				$scope.brandList = response;
            }
        );
    }

    $scope.image_entity = {};
    //上传图片
    $scope.uploadFile = function () {
		uploadService.uploadFile().success(
			function (response) {
				if(response.success){
					$scope.image_entity ={url:response.message};
				}else{
                    alert(response.message);
				}
            }
		);
    }

    $scope.entity={goods:{},goodsDesc:{itemImages:[]	,specificationItems:[]}};
    //将当前的图片实体放入该方法
    $scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }


    //列表中移除图片
    $scope.remove_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }


    //读取一级分类
	$scope.selectItemCat1List = function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List = response;
            }
		);
    }

    //读取二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		//根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat2List=response;
            }
        );

        $scope.itemCat3List = {};
    });

    //读取三级分类
    $scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat3List=response;
            }
        );
    });


    //三级分类选择后  读取模板ID
    $scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function(response){
                $scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
            }
        );
    });

	//模版ID选择后，更新品牌列表,跟新模版对象
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
				function (response) {
                    $scope.typeTemplate=response;//获取类型模板
                    $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
                }
		);

        //查询规格列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        );

    });
	
	$scope.updateSpecAttribute=function ($event,name,value) {

		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if(object != null){
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1); //移除选项
				//如果选项都取消了，将此条记录移除
				if(object.attributeValue.length == 0){
					$scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object),1
					)
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push(
                {"attributeName":name,"attributeValue":[value]}
			);
		}
    }

});	
