import request from '@/utils/request';
import service from './serviceMap';
/* 小组维度 */
export async function queryIamsGroupChart(params) {
  return request(service.feds.groupStatistics, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
/* 个人维度 */
export async function queryIamsUserChart(params) {
  return request(service.feds.userStatistics, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
