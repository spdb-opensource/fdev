const mockjs = require('mockjs');

const releaseNode = {
  id: '@id',
  release_date: '@date',
  release_node_name: '@string',
  create_user: '@string',
  create_time: '@date',
  owner_groupId: '@id',
  release_manager: '@id',
  release_spdb_manager: '@id',
  release_spdb_no: '@string',
  uat_env_name: '@string',
  release_branch: '@string',
  status: '1',
  'task_status|1': ['2']
};

module.exports = {
  'POST /frelease/api/releasenode/queryReleaseNodes': mockjs.mock({
    'list|5': [releaseNode]
  })['list'],
  'POST /frelease/api/releasenode/queryDetailByTaskId': mockjs.mock(
    releaseNode
  ),
  'POST /frelease/api/releasenode/task/add': mockjs.mock(releaseNode),

  'POST /frelease/api/production/delete': mockjs.mock({
    id: '@id'
  }),
  'POST /frelease/api/production/add': mockjs.mock(releaseNode),
  'POST /frelease/api/production/update': mockjs.mock(releaseNode)
};
