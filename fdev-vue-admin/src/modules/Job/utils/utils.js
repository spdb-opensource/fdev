import { createJobModel, perform } from './constants';

export function formatJob(job) {
  const { review } = job;
  return {
    ...createJobModel(),
    ...job,
    unitNo: job.fdev_implement_unit_no,
    group: job.group,
    stage: perform.stages.find(stage => stage.value === job.stage),
    partyA: job.spdb_master,
    partyB: job.master,
    desc: job.desc,
    devStartDate: job.plan_start_time,
    sitStartDate: job.plan_inner_test_time,
    productionDate: job.plan_fire_time,
    actualProductionDate: job.fire_time,
    branch: job.feature_branch,
    review: {
      ...review,
      ...(review
        ? {
            db: review.data_base_alter,
            data_base_alter: review.data_base_alter,
            commonProfile: review.commonProfile,
            securityTest: review.securityTest,
            specialCase: review.specialCase,
            other_system: review.other_system
            // firewall: review.fire_wall_open,
            // interface: review.interface_alter,
            // script: review.script_alter,
            // asset: review.static_resource
          }
        : createJobModel().review)
    },
    watcher: job.concern,
    uatStartDate: job.plan_uat_test_start_time,
    uatEndDate: job.plan_uat_test_stop_time,
    relStartDate: job.plan_rel_test_time,
    releaseNode: {},
    maintask: perform.mainTasks.find(ele => job.maintask === ele.value) || null
  };
}

export function formatMyJob(job, userList) {
  const { review } = { ...job };
  for (let key in review) {
    Array.isArray(review[key]) &&
      review[key].map(ele => {
        if (ele.name) {
          ele.label = ele.name;
        }
      });
  }
  return {
    ...job,
    developer: getUsers(job.developer, userList),
    tester: getUsers(job.tester, userList),
    partyB: getUsers(job.partyB, userList),
    partyA: getUsers(job.partyA, userList)
  };
}

function getUsers(list, userList) {
  let users = [];
  if (Array.isArray(list)) {
    list.forEach(ele => {
      let user = userList.find(user => {
        return ele.id === user.id;
      });
      if (user) {
        users.push(user);
      }
    });
  }
  return users;
}
