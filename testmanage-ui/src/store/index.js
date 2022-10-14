import Vue from 'vue';
import Vuex from 'vuex';
import menu from './menu';
import reportForms from './reportForms';
import workOrderForm from '../pages/WorkOrder/store/workOrderForm';
import adminForm from '../pages/Admin/store/adminForm.js';
import chartsForm from '../pages/Charts/store/chartsForm.js';
import autoAssignForm from '../pages/AutoAssign/index';
import mantisForm from '../pages/Mantis/mantisForm';
import testCaseForm from '../pages/TestCase/testCaseForm';
import testPlanForm from '../pages/TestPlan/testPlanForm';
import autoTestForm from '../pages/AutoTest/store/autoTest';
Vue.use(Vuex);

const modules = {
  menu,
  reportForms,
  workOrderForm,
  adminForm,
  chartsForm,
  autoAssignForm,
  mantisForm,
  testCaseForm,
  testPlanForm,
  autoTestForm
};
export default new Vuex.Store({
  modules
});
