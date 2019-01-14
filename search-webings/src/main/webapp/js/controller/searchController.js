app.controller('searchController',function ($scope,searchService) {

    //定义搜索对象的结构
    $scope.searchMap = {'keywords':'','category':'','brand':'','spec':{}};

    

    $scope.search=function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
            }
        );
    }

    //添加搜索项，
    $scope.addSearchItem=function (key,value) {
        if(key == 'category'||key == 'brand'){ //如果用户点击的时品牌或分类
            $scope.searchMap[key] = value;
        }else{//用户点击的时规格
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//查询
    }
    
    //删除搜索项
    $scope.removeSearchItem=function (key) {
        if(key == 'category'||key == 'brand'){ //如果用户点击的时品牌或分类
            $scope.searchMap[key] = '';
        }else{//用户点击的时规格,需要把属性删掉
           delete $scope.searchMap.spec[key] ;
        }
        $scope.search();//查询
    }
});