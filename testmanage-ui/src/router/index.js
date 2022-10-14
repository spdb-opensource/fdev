import Vue from 'vue';
import Router from 'vue-router';
import axios from 'axios';
import { oauthGetAuthorization } from '@/services/login';
import { getUserRole } from '@/common/utlis';

const BasicLayout = () => import('@/layout/BasicLayout');
const Login = () => import('@/pages/login/Login');
const TestOrder = () => import('@/pages/WorkOrder/TestOrder');
const TaskList = () => import('@/pages/WorkOrder/TaskList');
const Plan = () => import('@/pages/TestPlan/Plan');
const Reuse = () => import('@/pages/TestPlan/Reuse');
const CaseBase = () => import('@/pages/TestCase/CaseBase');
const UserAdmin = () => import('@/pages/Admin/UserAdmin');
const FunctionMenu = () => import('@/pages/Admin/FunctionMenu');
const GeneralApproval = () => import('@/pages/Admin/GeneralApproval');
const DiscardApproval = () => import('@/pages/Admin/DiscardApproval');
const PassApproval = () => import('@/pages/Admin/PassApproval');
const EffectiveApproval = () => import('@/pages/Admin/EffectiveApproval');
const Notice = () => import('@/pages/Admin/Notice');
const About = () => import('@/pages/Admin/About');
const testReportSIT = () => import('@/pages/Admin/testReportSIT');
const Message = () => import('@/pages/Admin/Message');
const PrintContent = () => import('@/pages/Admin/PrintContent');
const SubmittedText = () => import('@/pages/Admin/SubmittedText/List');
const SubmittedTextProfile = () =>
  import('@/pages/Admin/SubmittedText/Profile');
const HistoryOrder = () => import('@/pages/WorkOrder/HistoryOrder');
const QueryOrder = () => import('@/pages/WorkOrder/QueryOrder');
const TestCaseExecute = () => import('@/pages/TestCase/TestCaseExecute');
const UserChart = () => import('@/pages/Charts/UserChart');
const OrderChart = () => import('@/pages/Charts/OrderChart');
const DefectChart = () => import('@/pages/Charts/defectChart');
const ApprovalOrder = () => import('@/pages/WorkOrder/ApprovalOrder');
const WasteOrder = () => import('@/pages/WorkOrder/WasteOrder');
const GroupChart = () => import('@/pages/Charts/GroupChart');
const QualityChart = () => import('@/pages/Charts/QualityChart');
const MantisIssue = () => import('@/pages/Mantis/MantisIssue');
const ProductionsProblem = () => import('@/pages/Mantis/ProductionsProblem');
const TestData = () => import('@/pages/DataMenu/TestData');
const SystemContact = () => import('@/pages/DataMenu/SystemContact');
const CreditCardOL = () => import('@/pages/DataMenu/CreditCardOL');
const ESBOL = () => import('@/pages/DataMenu/ESBOL');
const MantisInfo = () => import('@/pages/Mantis//MantisInfo');
const AssignOrder = () => import('@/pages/AutoAssign//AssignOrder');
const UserProfile = () => import('@/pages/Admin/UserProfile');

const AssertData = () => import('@/pages/AutoTest/AssertData');
const CaseDetail = () => import('@/pages/AutoTest/CaseDetail');
const ComponentData = () => import('@/pages/AutoTest/ComponentData');
const ComponentDetail = () => import('@/pages/AutoTest/ComponentDetail');
const ElementDictionary = () => import('@/pages/AutoTest/ElementDictionary');
const ElementPosition = () => import('@/pages/AutoTest/ElementPosition');
const MenuInfo = () => import('@/pages/AutoTest/MenuInfo');
const TestCase = () => import('@/pages/AutoTest/TestCase');
const TestDataAuto = () => import('@/pages/AutoTest/TestData');
const UserInfo = () => import('@/pages/AutoTest/UserInfo');

Vue.use(Router);

axios.defaults.withCredentials = true;
Vue.prototype.$http = axios;
Vue.prototype.$http = axios.create({});

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/Login',
    name: 'Login',
    component: Login,
    meta: {
      name: '登录'
    }
  },
  {
    path: '/MantisInfo',
    name: 'MantisInfo',
    meta: {
      requireAuth: true,
      name: ''
    },
    component: MantisInfo
  },

  {
    path: '/Torder',
    component: BasicLayout,
    name: 'testPlatform',
    redirect: 'TestOrder',
    children: [
      {
        path: '/TestOrder',
        name: 'TestOrder',
        meta: {
          requireAuth: true,
          name: '我的工单'
        },
        component: TestOrder
      },
      {
        path: '/TaskList',
        name: 'TaskList',
        meta: {
          requireAuth: true,
          name: '子任务列表'
        },
        component: TaskList
      },
      {
        path: '/Plan',
        name: 'Plan',
        meta: {
          requireAuth: true,
          name: '执行计划'
        },
        component: Plan
      },
      {
        path: '/CaseBase',
        name: 'CaseBase',
        meta: {
          requireAuth: true,
          name: '案例库'
        },
        component: CaseBase
      },
      {
        path: '/UserAdmin',
        name: 'UserAdmin',
        meta: {
          requireAuth: true,
          name: '用户管理'
        },
        component: UserAdmin
      },
      {
        path: '/FunctionMenu',
        name: 'FunctionMenu',
        meta: {
          requireAuth: true,
          name: '功能菜单'
        },
        component: FunctionMenu
      },
      {
        path: '/GeneralApproval',
        name: 'GeneralApproval',
        meta: {
          requireAuth: true,
          name: '通用审批'
        },
        component: GeneralApproval
      },
      {
        path: '/DiscardApproval',
        name: 'DiscardApproval',
        meta: {
          requireAuth: true,
          name: '废弃审批'
        },
        component: DiscardApproval
      },
      {
        path: '/PassApproval',
        name: 'PassApproval',
        meta: {
          requireAuth: true,
          name: '评审审批'
        },
        component: PassApproval
      },
      {
        path: '/EffectiveApproval',
        name: 'EffectiveApproval',
        meta: {
          requireAuth: true,
          name: '生效审批'
        },
        component: EffectiveApproval
      },
      {
        path: '/Notice',
        name: 'Notice',
        meta: {
          requireAuth: true,
          name: '公告'
        },
        component: Notice
      },
      {
        path: '/About',
        name: 'About',
        meta: {
          requireAuth: true,
          name: '关于'
        },
        component: About
      },
      {
        path: '/HistoryOrder',
        name: 'HistoryOrder',
        meta: {
          requireAuth: true,
          name: '历史工单'
        },
        component: HistoryOrder
      },
      {
        path: '/QueryOrder',
        name: 'QueryOrder',
        meta: {
          requireAuth: true,
          name: '工单查询'
        },
        component: QueryOrder
      },
      {
        path: '/reuse',
        name: 'Reuse',
        meta: {
          requireAuth: true,
          name: '案例复用'
        },
        component: Reuse
      },
      {
        path: '/TestCaseExecute',
        name: 'TestCaseExecute',
        meta: {
          requireAuth: true,
          name: ''
        },
        component: TestCaseExecute
      },
      {
        path: '/UserChart',
        name: 'UserChart',
        meta: {
          requireAuth: true,
          name: '人员维度报表'
        },
        component: UserChart
      },
      {
        path: '/OrderChart',
        name: 'OrderChart',
        meta: {
          requireAuth: true,
          name: '工单维度报表'
        },
        component: OrderChart
      },
      {
        path: '/DefectChart',
        name: 'DefectChart',
        meta: {
          requireAuth: true,
          name: '缺陷维度报表'
        },
        component: DefectChart
      },
      {
        path: '/GroupChart',
        name: 'GroupChart',
        meta: {
          requireAuth: true,
          name: '小组维度报表'
        },
        component: GroupChart
      },
      {
        path: '/QualityChart',
        name: 'QualityChart',
        meta: {
          requireAuth: true,
          name: '质量分析维度报表'
        },
        component: QualityChart
      },
      {
        path: '/testReportSIT',
        name: 'testReportSIT',
        meta: {
          requireAuth: true,
          name: 'SIT测试报告'
        },
        component: testReportSIT
      },
      {
        path: '/Message',
        name: 'Message',
        meta: {
          requireAuth: true,
          name: '消息'
        },
        component: Message
      },
      {
        path: '/PrintContent',
        name: 'PrintContent',
        meta: {
          requireAuth: true,
          name: 'SIT测试报告打印'
        },
        component: PrintContent
      },
      {
        path: '/sitMsg',
        name: 'SubmittedText',
        meta: {
          requireAuth: true,
          name: '提测信息'
        },
        component: SubmittedText
      },
      {
        path: '/sitMsg/:id',
        name: 'SubmittedTextProfile',
        props: route => ({
          id: route.params.id
        }),
        meta: {
          requireAuth: true,
          name: '提测信息详情'
        },
        component: SubmittedTextProfile
      },
      {
        path: '/ApprovalOrder',
        name: 'ApprovalOrder',
        meta: {
          requireAuth: true,
          name: '审批工单'
        },
        component: ApprovalOrder
      },
      {
        path: '/WasteOrder',
        name: 'WasteOrder',
        meta: {
          requireAuth: true,
          name: '废弃工单'
        },
        component: WasteOrder
      },
      {
        path: '/MantisIssue',
        name: 'MantisIssue',
        meta: {
          requireAuth: true,
          name: 'SIT缺陷'
        },
        component: MantisIssue
      },
      {
        path: '/ProductionsProblem',
        name: 'ProductionsProblem',
        meta: {
          requireAuth: true,
          name: '生产问题'
        },
        component: ProductionsProblem
      },
      {
        path: '/TestData',
        name: 'TestData',
        meta: {
          requireAuth: true,
          name: '测试数据'
        },
        component: TestData
      },
      {
        path: '/SystemContact',
        name: 'SystemContact',
        meta: {
          requireAuth: true,
          name: '系统联系人'
        },
        component: SystemContact
      },
      {
        path: '/CreditCardOL',
        name: 'CreditCardOL',
        meta: {
          requireAuth: true,
          name: '信用卡操作说明'
        },
        component: CreditCardOL
      },
      {
        path: '/ESBOL',
        name: 'ESBOL',
        meta: {
          requireAuth: true,
          name: 'ESB操作说明'
        },
        component: ESBOL
      },
      {
        path: '/AssignOrder',
        name: 'AssignOrder',
        meta: {
          requireAuth: true,
          name: '分配管理'
        },
        component: AssignOrder
      },
      {
        path: '/userProfile',
        name: 'UserProfile',
        meta: {
          requireAuth: true,
          name: '用户详情'
        },
        component: UserProfile
      },
      {
        path: '/AssertData',
        name: 'AssertData',
        meta: {
          requireAuth: true,
          name: '断言预期数据维护'
        },
        component: AssertData
      },
      {
        path: '/CaseDetail',
        name: 'CaseDetail',
        meta: {
          requireAuth: true,
          name: '案例明细维护'
        },
        component: CaseDetail
      },
      {
        path: '/ComponentData',
        name: 'ComponentData',
        meta: {
          requireAuth: true,
          name: '组件数据维护'
        },
        component: ComponentData
      },
      {
        path: '/ComponentDetail',
        name: 'ComponentDetail',
        meta: {
          requireAuth: true,
          name: '组件详情数据维护'
        },
        component: ComponentDetail
      },
      {
        path: '/ElementDictionary',
        name: 'ElementDictionary',
        meta: {
          requireAuth: true,
          name: '元素方法字典数据维护'
        },
        component: ElementDictionary
      },
      {
        path: '/ElementPosition',
        name: 'ElementPosition',
        meta: {
          requireAuth: true,
          name: '元素定位数据维护'
        },
        component: ElementPosition
      },
      {
        path: '/MenuInfo',
        name: 'MenuInfo',
        meta: {
          requireAuth: true,
          name: '菜单模块信息'
        },
        component: MenuInfo
      },
      {
        path: '/TestCase',
        name: 'TestCase',
        meta: {
          requireAuth: true,
          name: '测试案例维护'
        },
        component: TestCase
      },
      {
        path: '/TestDataAuto',
        name: 'TestDataAuto',
        meta: {
          requireAuth: true,
          name: '测试数据维护'
        },
        component: TestDataAuto
      },
      {
        path: '/UserInfo',
        name: 'UserInfo',
        meta: {
          requireAuth: true,
          name: '用户信息数据维护'
        },
        component: UserInfo
      }
    ]
  }
];

const router = new Router({
  routes
});

async function login(loginToken) {
  await oauthGetAuthorization({
    token: loginToken
  }).then(res => {
    sessionStorage.setItem('userInfo', JSON.stringify(res));
    let userRole = getUserRole();
    localStorage.setItem('userToken', res.userToken);
    sessionStorage.setItem('Trole', userRole);
    sessionStorage.setItem('TuserName', res.user_name_cn);
    sessionStorage.setItem('user_en_name', res.user_name_en);
    localStorage.setItem('user_en_name', res.user_name_en);
    sessionStorage.setItem('isAssessor', res.isAssessor);
    sessionStorage.setItem('userId', res.user_id);
    sessionStorage.setItem('mantisToken', res.mantis_token);
    sessionStorage.setItem('userRole', JSON.stringify(res.role));
    sessionStorage.setItem('groupName', res.group ? res.group.fullName : '');
    sessionStorage.setItem('group_id', res.group_id);
  });
}

router.beforeEach(async (to, from, next) => {
  if (to.path == '/TestOrder') {
    // 为了解决授权登录之后 重定向到TestOrder页面 Header组件先于TestOrder组件加载的问题  导航守卫之前就请求token
    let loginToken = to.query.token;
    if (loginToken) {
      if (sessionStorage.getItem('TuserName') == null) {
        await login(loginToken);
      }
    }
    if (
      localStorage.getItem('userToken') == null ||
      localStorage.getItem('userToken') == ''
    ) {
      if (loginToken) {
        await login(loginToken);
        next();
      } else {
        next({
          path: '/Login'
        });
      }
    }
  }

  if (to.matched.some(res => res.meta.requireAuth)) {
    //判断是否需要登陆权限
    //判断是否已经登录 既要判断localStorage又要判断sessionStorage
    if (
      localStorage.getItem('userToken') &&
      sessionStorage.getItem('TuserName')
    ) {
      next();
    } else {
      next({
        path: '/Login',
        query: {
          redirect: to.fullPath
        } //登录成功后重定向到当前页面
      });
    }
  } else {
    next();
  }
});

export default router;
