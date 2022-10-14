import request from '@/utils/request';
import service from './serviceMap';

// sonar-扫描时间、扫描版本
export async function projectAnalyses(params) {
  return request(service.fsonar.projectAnalyses, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function featureProjectAnalyses(params) {
  return request(service.fsonar.featureProjectAnalyses, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function scanningFeatureBranch(params) {
  return request(service.fsonar.scanningFeatureBranch, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function searchProject(params) {
  return request(service.fsonar.searchProject, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function componentTree(params) {
  return request(service.fsonar.componentTree, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getAnalysesHistory(params) {
  return request(service.fsonar.getAnalysesHistory, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getProjectInfos(params) {
  return request(service.fsonar.getProjectInfos, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function featureComponentTree(params) {
  return request(service.fsonar.featureComponentTree, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
export async function getProjectFeatureInfo(params) {
  return request(service.fsonar.getProjectFeatureInfo, {
    method: 'POST',
    data: {
      ...params
    }
  });
}
