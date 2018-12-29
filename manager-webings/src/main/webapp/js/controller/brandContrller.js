
app.controller('brandController',function ($scope,$controller,brandService) {

    $controller('baseController',{$scope:$scope});// 继承  $controller 也是angular提供的一个服务，
                                                 // 可以实现伪继承，实际上就是与BaseController共享$scope

    $scope.findAll = function(){
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }
    /*//读取列表数据绑定到表单中
    $scope.reloadList=function () {
        //debugger
        //切换页码
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,     //当前页码
        totalItems: 10,     //总条数
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50], //页码选项
        onChange: function(){
            $scope.reloadList();//重新加载 ,更改页面时触发事件,页面的触发点
        }
    };

    //分页查询
    $scope.findPage=function (page,rows) {
        brandService.findPage(page,rows).success(
            function (respose) {
                $scope.list = respose.rows;
                $scope.paginationConf.totalItems = respose.total;//更新总记录数
            }
        );
    }*/


    //保存和修改 可以根据id 判断是调用某个方法
    $scope.add = function () {
        var methodName = brandService.add($scope.entity);//方法名称
        if($scope.entity.id != null){
            methodName = brandService.update($scope.entity);
        }
        methodName.success(
            function (response) {
                if(response.success){
                    //重新查询
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        )
    }


    //根据id 查询品牌
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;

            }
        )
    }

    $scope.selectIds = [];

   /* //更新复选 id
    $scope.updateSelection = function ($event,id){

        if($event.target.checked){//如果是被选中，怎添加到数组中
            $scope.selectIds.push(id)
        }else{ //没有选中则删除id
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);//删除

        }
    }*/

    //根据Id删除品牌 ，批量删除
    $scope.dele = function () {

        //获取选中的复选框
        brandService.dele($scope.selectIds).success(
          function (response) {
              if(response.success){
                  $scope.reloadList();
              }

          }
        );
    }

    //条件查询
    $scope.search = function (page,rows) {
       // debugger
        brandService.search(page,rows,$scope.searchEntity).success(
            function (respose) {
                $scope.list = respose.rows;
                $scope.paginationConf.totalItems = respose.total;//更新总记录数
            }
        );
    }
});