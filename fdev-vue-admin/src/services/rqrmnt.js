import request from '@/utils/request';
import service from './serviceMap';

export async function queryAllDemands(params) {
  return request(service.frqrmnt.queryAllDemands, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
