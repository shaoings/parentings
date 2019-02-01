app.controller('searchController',function ($scope,$location,searchService) {

    //定义搜索对象的结构
    $scope.searchMap = {'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};

    
    //搜索
    $scope.search=function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
                //$scope.searchMap.pageNo = 1;//查询你后显示第一页
                buildPageLabel();//构建分页栏
            }
        );
    }

    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage = 1;//开始页码
        var lastPage = $scope.resultMap.totalpages;//截至页码
        $scope.firstDot =true;//前面有点
        $scope.lastDot = true;//后面有点

        if($scope.resultMap.totalpages>5){ //如果页码数量大于5
            if($scope.searchMap.pageNo<=3){//如果当前页码小于等于3，显示前5条
                lastPage = 5;
                $scope.firstDot = false;//前面没点
            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalpages - 2){
                firstPage = $scope.resultMap.totalpages-4;
                $scope.lastDot=false;//后面没点
            }else{//显示以当前页为中心
                firstPage = $scope.searchMap.pageNo-2;
                lastPage = $scope.searchMap.pageNo+2;
            }
        }

        //构建页码
        for(var i = firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //添加搜索项，
    $scope.addSearchItem=function (key,value) {
        if(key == 'category'||key == 'brand'||key=='price'){ //如果用户点击的时品牌或分类
            $scope.searchMap[key] = value;
        }else{//用户点击的时规格
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//查询
    }
    
    //删除搜索项
    $scope.removeSearchItem=function (key) {
        if(key == 'category'||key == 'brand'||key=='price'){ //如果用户点击的时品牌或分类
            $scope.searchMap[key] = '';
        }else{//用户点击的时规格,需要把属性删掉
           delete $scope.searchMap.spec[key] ;
        }
        $scope.search();//查询
    }

    //分页查询
    $scope.queryByPage = function (pageNo) {
        if(pageNo<1 || pageNo>$scope.resultMap.totalpages){
            return ;
        }

        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalpages){
            return true;
        }else{
            return false;
        }
    }

    //排序
    $scope.sortSearch=function (sortField,sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //判断关键字是否是品牌
    $scope.keywordsIsBrand = function () {
       // debugger
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //加载关键字
    $scope.loadKeywords=function () {
       $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();//查询
    }
});