class InitTable {
  // 获取元素
  _getContain(el) {
    return document.querySelector(el);
  }
  // 根据数据生成结构（参数1：小组数组, 参数2：列表数组对象, 参数3：元素容器, 参数4 列表类型）
  createDomByData(groupArr, list, el, type) {
    // 循环数据生成dom树
    groupArr.forEach(item => {
      this._renderDom(
        this._createNewGroupData(
          this._getGroupData(list, groupArr, type)[item.group],
          type
        ),
        this._getContain(el),
        item,
        type
      );
    });
  }
  // 组装key: 小组 value: 数据的map（1）
  _getGroupData(list, groupArr, type) {
    let obj = {};
    if (type === 'task') {
      groupArr.forEach(item => {
        obj[item.group] = list.filter(ctem => ctem.group === item.group);
      });
    }
    if (type === 'demand') {
      groupArr.forEach(item => {
        obj[item.group] = list.filter(
          ctem => ctem.demand_leader_group === item.group
        );
      });
    }
    return obj;
  }
  // 将组装的数据 按小组对应的数据传入（2）
  _createNewGroupData(list, type) {
    let modelArr = [];
    // 获取每一组对应的通过数据
    let passData = list.filter(item => item.finshFlag === 'pass');
    // 获取每一组对应的未通过数据
    let noPassData = list.filter(item => item.finshFlag === 'noPass');
    let groupNewData = passData.concat(noPassData);
    groupNewData.forEach(item => {
      this._createModel(modelArr, item, type);
    });
    // 返回组装后的数据
    return modelArr;
  }
  // 创建数据模板: 不管传过来的数据顺序如何都将准确渲染 (3)
  _createModel(model, list, type) {
    let obj = {};
    if (type === 'task') {
      obj = {
        group: list.group || '', // 小组名称
        finshFlag: list.finshFlag || '', // 是否通过
        redmine_id: list.redmine_id || '', // 需求编号
        name: list.name || '', // 任务名称
        positionStatus: list.positionStatus || '', // 卡点状态
        ifFinsh: list.finshFlag || '', // 完成情况
        currentStage: list.currentStage || '', // 当前阶段
        currentStageTime: list.currentStageTime || '', // 当前阶段开始时间
        checkCount: list.checkCount || '', // 审核次数
        uiVerifyReporter: list.uiVerifyReporter || '', // 开发人员
        plan_inner_test_time: list.plan_inner_test_time || '', // 计划提交内测日期
        plan_uat_test_start_time: list.plan_uat_test_start_time || '', // 计划提交业测日期
        start_inner_test_time: list.start_inner_test_time || '', // 实际提交内测日期
        start_uat_test_time: list.start_uat_test_time || '', // 实际提交业测日期
        implementLeaderNameCN:
          (list.implementLeaderNameCN && list.implementLeaderNameCN.join()) ||
          '', // 研发单元牵头人
        row: list // 当前行的所有数据
      };
    } else if (type === 'demand') {
      obj = {
        group: list.group || '', // 小组名称
        finshFlag: list.finshFlag || '', // 是否通过
        oa_contact_name: list.oa_contact_name || '', // 需求名称
        ifFinsh: list.finshFlag || '', // 完成情况
        currentStage: list.currentStage || '', // 当前阶段
        currentStageTime: list.currentStageTime || '', // 当前阶段开始时间
        checkCount: list.checkCount || '', // 审核次数
        uiVerifyReporter: list.uiVerifyReporter || '', // 开发人员
        plan_inner_test_time: list.plan_inner_test_time || '', // 计划提交内测日期
        plan_test_date: list.plan_test_date || '', // 计划提交业测日期
        real_inner_test_date: list.real_inner_test_date || '', // 实际提交内测日期
        real_test_date: list.real_test_date || '', // 实际提交业测日期
        row: list // 当前行的所有数据
      };
    }
    model.push(obj);
  }
  // 主要渲染结构入口 （4）
  _renderDom(groupData, box, groupMap, type) {
    // 添加通过、未通过为0情况的数据
    this._ifZero(groupData, type);
    for (let i = 0; i < groupData.length; i++) {
      let tr = document.createElement('tr');
      for (let key in groupData[i]) {
        // 创建一条数据的td
        if (this._ifIncludes().includes(key)) {
          switch (key) {
            case 'group':
              if (i === 0) {
                let text = groupMap.groupName;
                let len = groupData.length;
                this._cTd(text, key, len, tr, groupData, groupData[i]);
              }
              break;
            case 'finshFlag':
              if (this._Pass(groupData, i, key)) {
                let txt = `通过${this._passNums(groupData)}`;
                if (!groupData[i].group) txt = '通过0';
                // 生成通过数td
                this._cTd(
                  txt,
                  'pass1',
                  this._passNums(groupData),
                  tr,
                  groupData,
                  groupData[i]
                );
              }
              if (this._noPass(groupData, i, key)) {
                let txt = `未通过${this._noPassNums(groupData)}`;
                if (!groupData[i].group) txt = '未通过0';
                this._cTd(
                  txt,
                  'pass0',
                  this._noPassNums(groupData),
                  tr,
                  groupData,
                  groupData[i]
                );
              }
              break;
          }
        } else {
          if (key !== 'row') {
            this._cTd(
              `${groupData[i][key]}`,
              key,
              1,
              tr,
              groupData,
              groupData[i]
            );
          }
        }
        box.appendChild(tr);
      }
    }
    this._addSpace(box);
  }
  // 创建不同类型的Td (5)
  _cTd(text, type, len, p, groupData, rowInfo) {
    let newLen = len;
    // 创建需要修改文本的字段类型数组
    let updateTextArr = ['positionStatus', 'ifFinsh', 'currentStage'];
    let td = document.createElement('td');
    td.className = `${type}`;
    switch (type) {
      case 'group':
        td.rowSpan = len;
        td.style.width = '60px';
        td.style.backgroundColor = '#EFEFEF';
        td.style.borderBottom = 0;
        newLen = groupData.filter(item => item.group !== '').length;
        text = `${this._match(text)} </br></br> 总数</br>${newLen}`;
        break;
      case 'pass1':
        td.rowSpan = len;
        td.style.width = '50px';
        td.style.backgroundColor = 'rgba(77, 187, 89, 0.2)';
        td.style.borderBottom = 0;
        text = this._matchIfPass(text, '通过');
        break;
      case 'pass0':
        td.rowSpan = len;
        td.style.width = '50px';
        td.style.backgroundColor = 'rgba(239, 83, 80, 0.2)';
        td.style.borderBottom = 0;
        text = this._matchIfPass(text, '未通过');
        break;
      default:
        if (text) {
          this._setTdStyle(td, type);
          if (updateTextArr.includes(type)) {
            td.className += ` ${this._addClass(type, text)}`;
            text = this._updateText(type, text);
          }
        }
    }
    td.innerHTML = `${text}`;
    // 自定义属性缓存每行的数据
    td.rowInfo = JSON.stringify(rowInfo.row);
    let noTitle = ['pass1', 'pass0', 'group'];
    if (!noTitle.includes(type)) td.title = `${text}`;
    p.appendChild(td);
  }
  /**
   * 工具方法
   */
  // 是否包含以下字段
  _ifIncludes() {
    return ['group', 'finshFlag'];
  }
  // 小组通过数
  _passNums(list) {
    return list.filter(item => item.finshFlag === 'pass').length;
  }
  // 小组未通过数
  _noPassNums(list) {
    return list.filter(item => item.finshFlag === 'noPass').length;
  }
  // 判断是否是通过
  _Pass(groupData, i, key) {
    return (
      this._passNums(groupData) > 0 && i === 0 && groupData[i][key] === 'pass'
    );
  }
  // 判断是否是未通过
  _noPass(groupData, i, key) {
    return (
      this._noPassNums(groupData) > 0 &&
      groupData.length - this._noPassNums(groupData) === i &&
      groupData[i][key] === 'noPass'
    );
  }
  // 每个小组添加间距
  _addSpace(box) {
    let space = document.createElement('tr');
    space.style.height = '20px';
    space.style.borderTop = '1px solid #ccc';
    box.appendChild(space);
  }
  // 小组名称换行
  _match(str) {
    return str.split('').join('</br>');
  }
  // 小组通过数 未通过数 中文和数字换行
  _matchIfPass(str, pass) {
    let reg = /[\u4e00-\u9fa5]+/g;
    return pass + str.split(reg).join('</br>');
  }
  // 深度拷贝
  _deepCopy(obj) {
    return JSON.parse(JSON.stringify(obj));
  }
  // 为td设置宽度
  _setTdStyle(td, type) {
    let obj = {
      redmine_id: '182px', // 需求编号
      name: '151px', // 任务名称
      positionStatus: '68.5px', // 卡点状态
      ifFinsh: '115px', // 完成情况
      currentStage: '115px', // 当前阶段
      currentStageTime: '150px', // 当前阶段开始时间
      checkCount: '70px',
      uiVerifyReporter: '70px',
      plan_inner_test_time: '150px',
      plan_uat_test_start_time: '150px',
      start_inner_test_time: '150px',
      start_uat_test_time: '150px',
      implementLeaderNameCN: '80px'
    };
    return (td.style.width = obj[type]);
  }
  // 为td添加类名
  _addClass(type, text) {
    let obj = {
      positionStatus: {
        fail: 'fail',
        ok: 'ok'
      },
      ifFinsh: {
        pass: 'pass',
        noPass: 'noPass'
      },
      currentStage: {
        alloting: 'alloting',
        checking: 'checking',
        updateing: 'updateing'
      }
    };
    return obj[type][text];
  }
  // td是否需要更新文本
  _updateText(type, text) {
    let obj = {
      positionStatus: {
        fail: '失败',
        ok: '正常'
      },
      ifFinsh: {
        pass: '通过',
        noPass: '未通过'
      },
      currentStage: {
        alloting: '分配中',
        checking: '审核中',
        updateing: '修改中'
      }
    };
    return obj[type][text];
  }
  // 当通过数为0 或者 未通过数为0 或者两者都为0时
  _ifZero(list, type) {
    let obj;
    if (type === 'task') {
      obj = {
        group: '', // 小组名称
        finshFlag: '', // 是否通过
        redmine_id: '', // 需求编号
        name: '', // 任务名称
        positionStatus: '', // 卡点状态
        ifFinsh: '', // 完成情况
        currentStage: '', // 当前阶段
        currentStageTime: '', // 当前阶段开始时间
        checkCount: '', // 审核次数
        uiVerifyReporter: '', // 开发人员
        plan_inner_test_time: '', // 计划提交内测日期
        plan_uat_test_start_time: '', // 计划提交业测日期
        start_inner_test_time: '', // 实际提交内测日期
        start_uat_test_time: '', // 实际提交业测日期
        implementLeaderNameCN: '' // 研发单元牵头人
      };
    } else {
      obj = {
        group: list.group || '', // 小组名称
        finshFlag: list.finshFlag || '', // 是否通过
        oa_contact_name: list.oa_contact_name || '', // 需求名称
        ifFinsh: list.finshFlag || '', // 完成情况
        currentStage: list.currentStage || '', // 当前阶段
        currentStageTime: list.currentStageTime || '', // 当前阶段开始时间
        checkCount: list.checkCount || '', // 审核次数
        uiVerifyReporter: list.uiVerifyReporter || '', // 开发人员
        plan_inner_test_time: list.plan_inner_test_time || '', // 计划提交内测日期
        plan_uat_test_start_time: list.plan_uat_test_start_time || '', // 计划提交业测日期
        start_inner_test_time: list.start_inner_test_time || '', // 实际提交内测日期
        start_uat_test_time: list.start_uat_test_time || '', // 实际提交业测日期
        row: list // 当前行的所有数据
      };
    }
    // 通过 未通过都为0
    if (list.length === 0) {
      obj.finshFlag = 'pass';
      list.push(this._deepCopy(obj));
      obj.finshFlag = 'noPass';
      list.push(this._deepCopy(obj));
      return list;
    }
    // 通过为0
    if (this._passNums(list) === 0) {
      obj.finshFlag = 'pass';
      list.unshift(this._deepCopy(obj));
      return list;
    }
    // 未通过为0
    if (this._noPassNums(list) === 0) {
      obj.finshFlag = 'noPass';
      list.push(this._deepCopy(obj));
      return list;
    }
    return list;
  }
}
export default new InitTable();
