关于PageHelper分⻚中当前⻚、每⻚数、总条数混乱问题 正确⽤法： PageHelper.startPage(pageReq.getPage(), pageReq.getSize(); userList =userMaper.selectUserByOrgId(orgIdList, vo.getStatusList(); PageInfo pageInfo =newPageInfo<>(userList); List<UserVo> list =newArayList<>(); list = userList.stream().map({

.

}) pageInfo.setList(list); returnpageInfo;

PageHelper.startPage(pageReq.getPage(), pageReq.getSize(); 这个⼀定要直接放在maper查询上⽅，不然传递当前⻚，每⻚数可能会有问题 PageInfo pageInfo =newPageInfo<>(userList); userList查询的数据直接填充PageInfo

错误⽤法 PageHelper.startPage(pageReq.getPage(), pageReq.getSize(); userList =userMaper.selectUserByOrgId(orgIdList, vo.getStatusList();

List<UserVo> list =newArayList<>(); list = userList.stream().map({

.

}) PageInfo pageInfo =newPageInfo<>(list);/放在后⾯填充list，会导致PageInfo当前⻚、每⻚数被覆 盖。 returnpageInfo;

