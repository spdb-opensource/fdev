已经完成的接口大致分为以下五类

1. 用户相关接口

   ```
   /getFevUsers get
   调用fdev-user模块下的/api/user/query接口，返回所有用户信息

   /getGitLabUsers get
   获取已经同步到本地的人员信息，查询gitlabUserInfo人员信息 

   /selectUserAndSave
   从Fedv查询所有人员， 删除本地的用户信息， 将新的用户信息保存到本地 
   ```

   ​

2. 用户组相关接口

   ```
   /getFevGroups get
   调用fdev-user模块下的/api/group/query接口，返回所有小组信息

   /getGitLabGroups get
   获取已经同步到本地的小组信息，查询gitlabGroupInfo小组信息 

   /selectGroupAndSave get
   从Fedv查询所有小组， 删除本地的小组信息， 将新的小组信息保存到本地 

   /stepGroups get
   查询gitlabGroupInfo小组信息，拼接为三级返回到页面展示
   ```

   ​

3. 应用相关接口

   ```
   /getFevApps get
   调用fdev-app模块下的/api/app/query接口，返回所有应用信息

   /getGitLabApps get
   获取已经同步到本地的应用信息，查询gitlabProjectInfo应用信息 

   /selectAppAndSave get
   从Fedv查询所有应用， 删除本地的应用信息， 将新的应用信息保存到本地 
   ```

   ​

4. 获取gitlab所有commit提交记录

   ```
   /addCommitInfoList get
   example：http://localhost:8080/addCommitInfoList?since=2019-08-09&until=2019-08-14
   获取gitlab上在2019-08-09到2019-08-14的相关提交记录保存到本地gitlabCommitInfo中
   ```

   ​

5. 将commit信息汇总

   ```
   /addGitlabCommitInfo
   example：http://localhost:8080/addGitlabCommitInfo?startDate=2019-08-09&endDate=2019-08-13
   将gitlabCommitInfo表中的提交记录在2019-08-09和2019-08-13直接的记录汇总到gitlabWorkInfo中，提交记录为每人每天的所有提交记录汇总。

   /getGitlabCommitInfoByGroupUser
   查询commit汇总信息，根据groupId查询当前组下的所有用户，再根据用户查询汇总信息
   ```

   ​