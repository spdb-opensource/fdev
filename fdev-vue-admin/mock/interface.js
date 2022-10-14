const mockjs = require('mockjs');
const { wrap7A, wrapError } = require('./utils/utils');

const list = {
  apiJsons: [
    {
      appId: '5d366e47972bb00010e28a41',
      branch: 'master',
      id: '5d258813034c6f0010845c2f',
      interfaceDescription: '查询产品信息展示',
      interfaceName: '查询产品信息展示',
      isNew: 0,
      reqHeader: [
        {
          name: 'ExtendContent',
          description: '扩展内容',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      request: [
        {
          name: 'Channel',
          description: '渠道标志',
          type: 'string',
          required: 'false'
        }
      ],
      requestProtocol: 'http',
      requestType: 'json/wsdl',
      response: [
        { name: 'LoopResult', description: '查询产品信息展示', type: 'array' }
      ],
      rspHeader: [
        {
          name: 'ReturnCode',
          description: '交易响应码',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      serviceId: 'ims-product-gold',
      transId: 'QueryRealGoldProductInfoDisplay',
      uri: 'http://ims-product-gold/rest/QueryRealGoldProductInfoDisplay',
      ver: 0
    },
    {
      appId: '5d356c83972bb00010e28a3e',
      branch: 'master',
      id: '5d356ace0ced79157476abff',
      interfaceDescription:
        'http://xxx/ebank/CommonServices/product/ims-product-finance/blob/snapshot/ims-project-finance/src/main/resources/config/spdb/mybatis/mapper/per/sql_common.xml: Queryevtype',
      interfaceName: '根据理财产品编号查询产品的详细信息接口',
      isNew: 0,
      reqHeader: [
        {
          name: 'ExtendContent',
          description: '扩展内容',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      request: [
        {
          name: 'FinanceNo',
          description: '产品编号',
          type: 'string',
          required: 'true'
        }
      ],
      requestProtocol: 'http',
      requestType: 'json/wsdl',
      response: [
        {
          name: 'InfoType',
          description: '产品类型',
          type: 'string',
          required: 'false'
        }
      ],
      rspHeader: [
        {
          name: 'ReturnCode',
          description: '交易响应码',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      serviceId: 'ims-product-finance',
      transId: 'Queryevtype',
      uri: 'http://ims-product-finance/rest/Queryevtype',
      ver: 0
    },
    {
      appId: '5d356c83972bb00010e28a3e',
      branch: 'master',
      id: '5d356ace0ced79157476ac00',
      interfaceDescription: '净值查询',
      interfaceName: '净值查询',
      isNew: 0,
      reqHeader: [
        {
          name: 'ExtendContent',
          description: '扩展内容',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      request: [
        {
          name: 'FinanceSort',
          description: '排序标识',
          type: 'string',
          required: 'false'
        }
      ],
      requestProtocol: 'http',
      requestType: 'json/wsdl',
      response: [{ name: 'List', description: '列表数据', type: 'array' }],
      rspHeader: [
        {
          name: 'ReturnCode',
          description: '交易响应码',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      serviceId: 'ims-product-finance',
      transId: 'QueryFinanceSearch',
      uri: 'http://ims-product-finance/rest/QueryFinanceSearch',
      ver: 0
    }
  ],
  total: 230
};

const interfaceModel = {
  id: '@id',
  interfaceName: '@first',
  serviceId: '@string',
  serviceConsumer: 'feature',
  transId: '@id',
  'branch|1': ['master', 'release'],
  uri: '@url',
  requestType: 'json/wsdl',
  requestProtocol: 'http',
  interfaceDescription: '@string',
  version: '0',
  requestParams: [
    {
      paramName: 'MasterId',
      paramDesc: '客户号',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'PayeeName',
      paramDesc: '收款人姓名',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    }
  ],
  responseParams: [
    {
      paramName: 'name',
      paramDesc: '姓名',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'age',
      paramDesc: '年龄',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'list',
      paramDesc: '集合',
      paramType: 'Array',
      isRequired: '0',
      desc: '描述信息',
      paramValue: [
        { paramName: 'MasterId', paramDesc: '客户号', paramType: 'String' },
        { paramName: 'AccountId', paramDesc: 'id', paramType: 'String' },
        {
          paramName: 'PayeeName',
          paramDesc: '收款人名称',
          paramType: 'String'
        },
        { paramName: 'PayeeNote', paramDesc: '备注', paramType: 'String' },
        {
          paramName: 'PayeeAccountType',
          paramDesc: '账号类型（1:银行卡、2:email、3:手机号、4:地址）',
          paramType: 'String'
        },
        {
          PayeeType: 'PayeeName',
          paramDesc: '(账户类型)0-公司账户，1-借记卡，2-活期一本通，U-信用卡',
          paramType: 'String'
        }
      ]
    }
  ],
  requestHeaders: [
    {
      paramName: 'ExtendContent',
      paramDesc: '扩展内容',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'PIN',
      paramDesc: 'PIN种子',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'BranchId',
      paramDesc: '虚拟机构号',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    }
  ],
  responseHeaders: [
    {
      paramName: 'ReturnCode',
      paramDesc: '交易响应码',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'ReturnMsg',
      paramDesc: '交易响应信息',
      paramType: 'String',
      isRequired: '0',
      desc: '描述信息'
    },
    {
      paramName: 'BackendSeqNo',
      paramDesc: '后台交易流水号',
      paramType: 'Json',
      isRequired: '1',
      desc: '描述信息'
    }
  ]
};

const versions = {
  id: '@id',
  transId: 'QueryCardInfo',
  serviceId: 'ims-user-perinfo',
  interfaceName: '查询客户号卡信息',
  ver: '@natural(0, 1)',
  md5: '367f889cbbae5da7464f7793ddae2e7a',
  isNew: 0,
  branch: 'master',
  lastBranch: 0,
  createTime: '',
  reqHeader: {
    type: 'object',
    required: true,
    properties: {
      ExtendContent: {
        description: '扩展内容',
        type: 'string',
        required: true,
        maxLength: 128
      },
      PIN: {
        description: 'PIN种子',
        type: 'string',
        required: true,
        maxLength: 16
      },
      KeyVersionNo: {
        description: '密钥版本号',
        type: 'string',
        required: true,
        maxLength: 10
      },
      TranTellerNo: {
        description: '交易柜员代码',
        type: 'string',
        required: false,
        maxLength: 8
      },
      TranSeqNo: {
        description: '交易流水号',
        type: 'string',
        required: false,
        maxLength: 26
      },
      BranchId: {
        description: '虚拟机构号',
        type: 'string',
        required: true,
        maxLength: 6
      }
    }
  },
  rspHeader: {
    type: 'object',
    required: true,
    properties: {
      ReturnCode: {
        description: '交易响应码',
        type: 'string',
        required: true,
        maxLength: 128
      },
      ReturnMsg: {
        description: '交易响应信息',
        type: 'string',
        required: true,
        maxLength: 16
      },
      BackendSeqNo: {
        description: '后台交易流水号',
        type: 'string',
        required: false,
        maxLength: 26
      }
    }
  },
  request: {
    $schema: 'http://json-schema.org/draft-03/schema',
    type: 'object',
    required: true,
    properties: {
      ReqBody: {
        type: 'object',
        required: true,
        properties: {
          MasterId: {
            description: '客户号',
            type: 'string',
            required: true
          },
          AcctNo: {
            description: '卡号',
            type: 'string',
            required: false
          }
        }
      }
    }
  },
  response: {
    $schema: 'http://json-schema.org/draft-03/schema',
    type: 'object',
    required: true,
    properties: {
      RspBody: {
        type: 'object',
        required: true,
        properties: {
          LoopResult: {
            description: '列表数据',
            type: 'array',
            required: true,
            items: {
              type: 'object',
              properties: {
                MasterId: {
                  description: '客户号',
                  type: 'string',
                  required: false
                },
                AcctNo: {
                  description: '卡号',
                  type: 'string',
                  required: false
                },
                AcctType: {
                  description: '卡类型',
                  type: 'string',
                  required: false
                },
                AcctNickName: {
                  description: '卡别名',
                  type: 'string',
                  required: false
                },
                AcctStatus: {
                  description: '卡状态',
                  type: 'string',
                  required: false
                },
                AcctDefault: {
                  description: '卡默认标识',
                  type: 'string',
                  required: false
                }
              }
            }
          }
        }
      }
    }
  },
  metadata: {
    ServiceId: 'ims-user-perinfo',
    InterfaceName: '查询客户号卡信息',
    Uri: 'http://ims-user-perinfo/rest/QueryCardInfo',
    RequestType: 'json/wsdl',
    RequestProtocol: 'http',
    InterfaceDescription: '查询客户号卡信息'
  }
};

const release = {
  total: 88,
  interfaceRelations: [
    {
      callingParty: {
        callingParty: 'mspmk-web-stockfuture',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-user-perinfo',
        id: '5cc6c197c8124300060f68ca'
      },
      branchName: 'master',
      id: '5d010f4e1c66941814000fa0',
      interfaceName: '查询客户号卡信息',
      transactionCode: 'QueryCardInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-stockfuture',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-user-perinfo',
        id: '5cc6c197c8124300060f68ca'
      },
      branchName: 'master',
      id: '5d010f4f1c66941814000fad',
      interfaceName: '查询用户理财经理信息',
      transactionCode: 'QueryUserManagerInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-stockfuture',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-param-basics ',
        id: ''
      },
      branchName: 'master',
      transactionCode: 'QueryEsbOpenInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-stockfuture',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-param-biz',
        id: ''
      },
      branchName: 'master',
      transactionCode: 'QuerySupportStockCompanyInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-stockfuture',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-param-biz',
        id: ''
      },
      branchName: 'master',
      transactionCode: 'QrySecurityPrl'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-noblemetals',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-user-perinfo',
        id: '5cc6c197c8124300060f68ca'
      },
      branchName: 'master',
      id: '5d010f4e1c66941814000fa0',
      interfaceName: '查询客户号卡信息',
      transactionCode: 'QueryCardInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-noblemetals',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-user-perinfo',
        id: '5cc6c197c8124300060f68ca'
      },
      branchName: 'master',
      id: '5d010f4f1c66941814000fad',
      interfaceName: '查询用户理财经理信息',
      transactionCode: 'QueryUserManagerInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-noblemetals',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-param-basics',
        id: ''
      },
      branchName: 'master',
      transactionCode: 'QueryEsbOpenInfo'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-noblemetals',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-product-gold',
        id: ''
      },
      branchName: 'master',
      id: '5d0109061c66941814000f70',
      interfaceName: '新增收货地址',
      transactionCode: 'AddMallReceiveAddr'
    },
    {
      callingParty: {
        callingParty: 'mspmk-web-noblemetals',
        id: ''
      },
      serviceProvider: {
        serviceProvider: 'ims-product-gold',
        id: ''
      },
      branchName: 'master',
      id: '5d0109061c66941814000f7b',
      interfaceName: '查询收货地址列表',
      transactionCode: 'QueryMallAddressList'
    }
  ]
};

const detail = {
  id: '5d010f4e1c66941814000fa0',
  transId: 'QueryCardInfo',
  serviceId: 'ims-user-perinfo',
  ver: 0,
  md5: '367f889cbbae5da7464f7793ddae2e7a',
  isNew: 0,
  interfaceName: '查询客户号卡信息',
  uri: 'http://ims-user-perinfo/rest/QueryCardInfo',
  requestType: 'json/wsdl',
  requestProtocol: 'http',
  interfaceDescription: '查询客户号卡信息',
  branch: 'master',
  tags: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  request: [
    {
      name: 'MasterId',
      description: '客户号',
      type: 'string',
      required: 'true'
    },
    {
      name: 'AcctNo',
      description: '卡号',
      type: 'string',
      required: 'false'
    }
  ],
  response: [
    {
      name: 'LoopResult',
      description: '列表数据',
      type: 'array',
      items: [
        {
          name: 'MasterId',
          description: '客户号',
          type: 'string',
          required: 'false'
        },
        {
          name: 'AcctNo',
          description: '卡号',
          type: 'string',
          required: 'false'
        },
        {
          name: 'AcctType',
          description: '卡类型',
          type: 'string',
          required: 'false'
        },
        {
          name: 'AcctNickName',
          description: '卡别名',
          type: 'string',
          required: 'false'
        },
        {
          name: 'AcctStatus',
          description: '卡状态',
          type: 'string',
          required: 'false'
        },
        {
          name: 'AcctDefault',
          description: '卡默认标识',
          type: 'string',
          required: 'false'
        }
      ],
      required: 'true'
    }
  ],
  reqHeader: [
    {
      name: 'ExtendContent',
      description: '扩展内容',
      type: 'string',
      required: 'true',
      maxLength: '128'
    },
    {
      name: 'PIN',
      description: 'PIN种子',
      type: 'string',
      required: 'true',
      maxLength: '16'
    },
    {
      name: 'KeyVersionNo',
      description: '密钥版本号',
      type: 'string',
      required: 'true',
      maxLength: '10'
    },
    {
      name: 'TranTellerNo',
      description: '交易柜员代码',
      type: 'string',
      required: 'false',
      maxLength: '8'
    },
    {
      name: 'TranSeqNo',
      description: '交易流水号',
      type: 'string',
      required: 'false',
      maxLength: '26'
    },
    {
      name: 'BranchId',
      description: '虚拟机构号',
      type: 'string',
      required: 'true',
      maxLength: '6'
    }
  ],
  rspHeader: [
    {
      name: 'ReturnCode',
      description: '交易响应码',
      type: 'string',
      required: 'true',
      maxLength: '128'
    },
    {
      name: 'ReturnMsg',
      description: '交易响应信息',
      type: 'string',
      required: 'true',
      maxLength: '16'
    },
    {
      name: 'BackendSeqNo',
      description: '后台交易流水号',
      type: 'string',
      required: 'false',
      maxLength: '26'
    }
  ]
};

const interfaceUrl = {
  interfacesUrl: 'xxx/project/131/interface/api/4582'
};

const queryInterface = {
  apiJsons: [
    {
      appId: '',
      branch: 'master',
      id: '5d258813034c6f0010845c2f',
      interfaceDescription: '查询产品信息展示',
      interfaceName: '查询产品信息展示',
      isNew: 0,
      reqHeader: [
        {
          name: 'ExtendContent',
          description: '扩展内容',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      request: [
        {
          name: 'Channel',
          description: '渠道标志',
          type: 'string',
          required: 'false'
        }
      ],
      requestProtocol: 'http',
      requestType: 'json/wsdl',
      response: [
        { name: 'LoopResult', description: '查询产品信息展示', type: 'array' }
      ],
      rspHeader: [
        {
          name: 'ReturnCode',
          description: '交易响应码',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      serviceId: 'ims-product-gold',
      transId: 'QueryRealGoldProductInfoDisplay',
      uri: 'http://ims-product-gold/rest/QueryRealGoldProductInfoDisplay',
      ver: 0
    },
    {
      appId: '',
      branch: 'master',
      id: '5d366ff618c8830010ea1b54',
      interfaceDescription: '订单流水号生成器',
      interfaceName: '订单流水号生成器',
      isNew: 0,
      reqHeader: [
        {
          name: 'ExtendContent',
          description: '扩展内容',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      request: [],
      requestProtocol: 'http',
      requestType: 'json/wsdl',
      response: [
        {
          name: 'SerialNo',
          description: '订单流水号071011',
          type: 'string',
          required: 'false'
        }
      ],
      rspHeader: [
        {
          name: 'ReturnCode',
          description: '交易响应码',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      serviceId: 'ims-product-gold',
      transId: 'QueryOrderId',
      uri: 'http://ims-product-gold/rest/QueryOrderId',
      ver: 0
    },
    {
      appId: '',
      branch: 'master',
      id: '5d366ff618c8830010ea1b55',
      interfaceDescription: '更新默认收货地址',
      interfaceName: '更新默认收货地址',
      isNew: 0,
      reqHeader: [
        {
          name: 'ExtendContent',
          description: '扩展内容',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      request: [
        { name: 'Name', description: '姓名', type: 'string', required: 'false' }
      ],
      requestProtocol: 'http',
      requestType: 'json/wsdl',
      response: [
        {
          name: 'Result',
          description: '更新结果',
          type: 'string',
          required: 'false'
        }
      ],
      rspHeader: [
        {
          name: 'ReturnCode',
          description: '交易响应码',
          type: 'string',
          required: 'true',
          maxLength: '128'
        }
      ],
      serviceId: 'ims-product-gold',
      transId: 'UpdateRealGoldRecvInfo',
      uri: 'http://ims-product-gold/rest/UpdateRealGoldRecvInfo',
      ver: 0
    }
  ]
};

const tags = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

const gitlab = ['1', '2', '3'];

const transList = {
  trans: [
    {
      transId: '12348i',
      interfaceName: '12348l',
      mock1: '12348j',
      serviceId: '12348l',
      branch: '12348j',
      channel: '12348l',
      writeJnl: '123489',
      isNeedLogin: '12348l',
      verCodeType: '1234h8',
      tag: '1234l8'
    }
  ],
  total: 40
};
const interfaceApplyodel = {
  id: '@id',
  applicant: '@cname',
  'serviceId|1': ['nbh-param-sso', '	ims-product-finance'],
  'serviceCalling|1': ['nbh-param-sso', '	ims-product-finance'],
  'transId|1': ['CTNT', 'MSTC', 'MSTU'],
  approver: '@cname',
  reason: '@string',
  refuseReason: '@string',
  'status|1': ['0', '1', '2']
};

const routerApplyModel = {
  name: '@string',
  projectName: '@string',
  'module|1': ['web', 'RN', 'Home'],
  path: './web/home/index.html',
  'branch|1': ['master', 'release', 'sit'],
  hash: 'test',
  appId: '@id'
};
const routerRelationModel = {
  name: '@string',
  targetProject: '@string',
  sourceProject: '@string',
  'branch|1': ['master', 'release', 'sit'],
  createTime: '@date',
  targetId: '@id',
  sourceId: '@id'
};
const routesDetail = {
  name: 'home',
  'module|1': ['web', 'RN', 'Home'],
  projectName: '@id',
  branch: 'sit',
  path: '/mspmk-wealth-home',
  authCheck: ['login', 'replace'],
  module: 'web',
  create_time: '@date',
  update_time: '@date',
  params: {
    test2: 'test2',
    test: 'test',
    'x-fullscreen': 'yes',
    test1: 'test1',
    test4: 'test1',
    test5: 'test1',
    test6: 'test1',
    teste: 'test1',
    test7: 'test1',
    test8: 'test1',
    test9: 'test1',
    testi: 'test1',
    testq: 'test1',
    testw: 'test1',
    testee: 'test1',
    testr: 'test1',
    testt: 'test1',
    testy: 'test1'
  },
  extra: [
    { entry: 'productlist', test2: 'test2', test3: 'test3', test1: 'test1' },
    {
      test2: 'test2list',
      st2: 'test2',
      test3: 'test3',
      test1: 'test1',
      testee: 'test2',
      test35: 'test3',
      test15: 'test1',
      test25: 'test2',
      test3t: 'test3',
      ee: 'ee',
      tt: 'rr'
    },
    { entry: 'productlist', test2: 'test2', test3: 'test3' }
  ],
  query: {
    test: {
      required: true
    },
    test2: {
      required: false
    },
    test3: {
      required: true
    }
  }
};
module.exports = {
  'POST /finterface/api/interface/queryInterfaceDetailById': mockjs.mock(
    detail
  ),
  'POST /finterface/api/interface/queryInterfaceListByAppId': mockjs.mock({
    'list|20': [interfaceModel]
  })['list'],
  'POST /finterface/api/interface/getInterfacesUrl': mockjs.mock(interfaceUrl),
  'POST /finterface/api/interface/transTags': mockjs.mock(tags),
  'POST /api/gitlabapi/getProjectBranchList': mockjs.mock(gitlab),
  'POST /finterface/api/interface/queryInterfacesList': mockjs.mock(transList),
  'POST /finterface/api/interfaceApplication/queryApplicationList': {
    total: 20,
    list: mockjs.mock({
      'list|20': [interfaceApplyodel]
    })['list']
  },
  'POST /finterface/api/interface/queryRoutes': {
    total: 20,
    list: mockjs.mock({
      'list|20': [routerApplyModel]
    })['list']
  },
  'POST /finterface/api/interface/queryRoutesRelation': {
    total: 20,
    list: mockjs.mock({
      'list|20': [routerRelationModel]
    })['list']
  },
  'POST /finterface/api/interface/queryRoutesDetail': mockjs.mock(routesDetail)
};
