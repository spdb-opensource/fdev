<template>
  <div class="container">
    <div class="text-center print-button noprint">
      <el-button @click="printSitReport">打印</el-button>
      <el-button @click="handleEmailDialogOpen">发送邮件</el-button>
    </div>
    <table class="tablecss" border="1" cellspacing="0">
      <tr class="tr-height bgcolor">
        <td colspan="1" class="item1">工单名称</td>
        <td colspan="3">{{ this.sitReportData.mainTaskName }}</td>
      </tr>
      <tr class="tr-height">
        <td>需求编号/实施单元编号</td>
        <td>{{ this.sitReportData.unitNo }}</td>
        <td>工单编号</td>
        <td>{{ this.reportTableData.workNo }}</td>
      </tr>
      <tr class="tr-height">
        <td>测试小组长</td>
        <td>{{ this.sitReportData.groupLeader }}</td>
        <td>测试人员</td>
        <td>{{ this.sitReportData.testers }}</td>
      </tr>
      <tr class="tr-height">
        <td colspan="1" class="item1">测试结果</td>
        <td colspan="3">{{ this.reportTableData.testResult }}</td>
      </tr>
      <tr class="tr-height">
        <td colspan="1" class="item1">测试范围</td>
        <td colspan="3">
          <p v-for="item in testRange" :key="item.testRange">
            {{ item.testRange }}
          </p>
        </td>
      </tr>
    </table>

    <table
      border="1"
      cellspacing="0"
      class="tablecss"
      :key="index"
      v-for="(item, index) in planData"
    >
      <tr class="tr-height bgcolor">
        <td colspan="1" class="item1">测试计划</td>
        <td colspan="3">{{ item.planName }}</td>
      </tr>
      <tr class="tr-height">
        <td class="item1">测试案例总数</td>
        <td>{{ item.allCase }}</td>
        <td class="item1">执行通过数</td>
        <td>{{ item.sumSucc }}</td>
      </tr>
      <tr class="tr-height">
        <td>案例执行失败数</td>
        <td>{{ item.sumFail }}</td>
        <td>案例执行阻塞数</td>
        <td>{{ item.sumBlock }}</td>
      </tr>
      <tr class="tr-height">
        <td>有效缺陷数</td>
        <td>{{ item.validMantis }}</td>
        <td>无效缺陷数</td>
        <td>{{ item.braceMantis }}</td>
      </tr>
      <tr class="tr-height">
        <td class="item1">设备信息</td>
        <td>{{ item.deviceInfo }}</td>
        <td>无效用例数</td>
        <td>{{ item.invalidCase }}</td>
      </tr>
      <tr class="tr-height">
        <td>计划开始时间</td>
        <td>{{ item.planStartDate }}</td>
        <td>计划结束时间</td>
        <td>{{ item.planEndDate }}</td>
      </tr>
    </table>

    <el-main class="chart-main">
      <el-row :gutter="10">
        <el-col :span="8" class=" bar-main" v-show="flawTypeData.length !== 0"
          ><div id="flawTypeName" class="chart-frame"></div
        ></el-col>
        <el-col :span="8" class=" bar-main" v-show="severityData.length !== 0"
          ><div id="severityName" class="chart-frame"></div
        ></el-col>
        <el-col :span="8" class=" bar-main" v-show="reporterData.length !== 0"
          ><div id="reporterName" class="chart-frame"></div
        ></el-col>
        <el-col :span="8" class=" bar-main" v-show="handlerData.length !== 0"
          ><div id="handlerName" class="chart-frame"></div
        ></el-col>
        <el-col :span="8" class=" bar-main" v-show="unitData.length !== 0"
          ><div id="unitName" class="chart-frame"></div
        ></el-col>
        <el-col :span="8" class=" bar-main" v-show="statusData.length !== 0"
          ><div id="statusName" class="chart-frame"></div
        ></el-col>
      </el-row>
    </el-main>

    <Dialog title="修改截图路径" :visible.sync="emailDialogOpened">
      <el-form label-position="left" label-width="30%">
        <el-form-item label="截图路径:">
          <el-input
            type="textarea"
            :autosize="{ minRows: 4 }"
            v-model="imageLink"
          />
        </el-form-item>
      </el-form>
      <el-button
        slot="footer"
        class="full-width"
        type="primary"
        @click="sendEmail"
      >
        确定
      </el-button>
    </Dialog>
    <!-- 安全测试 -->
    <Dialog title="上传文档" :visible.sync="isShowSafeTestMail">
      <el-upload
        style="margin-bottom:20px"
        :show-file-list="false"
        action=""
        accept=".docx"
        ref="upload"
        :http-request="uploadWord"
        :on-change="saveFile"
      >
        <el-button type="primary" icon="el-icon-upload">上传文档</el-button>
      </el-upload>
      <span v-if="wordDownLoadPath">
        <span
          >文档下载地址:<a :href="wordDownLoadPath">{{
            wordDownLoadPath
          }}</a></span
        >
      </span>
      <el-button
        slot="footer"
        class="full-width"
        type="primary"
        @click="sendEmail"
      >
        确定
      </el-button>
    </Dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { uploadDocFile } from '@/services/order.js';
export default {
  name: 'PrintContent',
  data() {
    return {
      wordDownLoadPath: '', //文档下载地址
      isShowSafeTestMail: false,
      testRange: [],
      sitReportData: {},
      reportTableData: {},
      flawTypeData: [],
      severityData: [],
      reporterData: [],
      handlerData: [],
      unitData: [],
      statusData: [],
      planData: [
        {
          allCase: '',
          sumUnExe: '',
          sumSucc: '',
          sumBlock: '',
          sumFail: '',
          ids: '',
          validMantis: '',
          braceMantis: '',
          planName: ''
        }
      ],
      buttonShow: true,
      emailDialogOpened: false,
      imageLink: ''
    };
  },
  computed: {
    ...mapState('adminForm', ['orderDetail'])
  },
  methods: {
    ...mapActions('adminForm', ['sendSitDoneMail', 'queryOrderByOrderNo']),
    queryOrder() {
      let res = this.sitReportData.res;
      this.reportTableData = res;
      let testRange = res.testRange.split('<br>');
      testRange.forEach((item, index) => {
        this.testRange.push({ testRange: testRange[index] });
      });
      this.planData = res.planData;
      if (res.flaw_type && res.flaw_type.length !== 0) {
        this.flawTypeData = res.flaw_type;
        this.drawPie('flawTypeName', this.flawTypeData, '缺陷类型');
      }
      if (res.severity && res.severity.length != 0) {
        this.severityData = res.severity;
        let i = '';
        let len = '';
        for (i = 0, len = this.severityData.length; i < len; i++) {
          if (this.severityData[i].name == '10') {
            this.severityData[i].name = '新功能';
          } else if (this.severityData[i].name == '20') {
            this.severityData[i].name = '细节';
          } else if (this.severityData[i].name == '30') {
            this.severityData[i].name = '文字';
          } else if (this.severityData[i].name == '40') {
            this.severityData[i].name = '小调整';
          } else if (this.severityData[i].name == '50') {
            this.severityData[i].name = '小错误';
          } else if (this.severityData[i].name == '60') {
            this.severityData[i].name = '很严重';
          } else if (this.severityData[i].name == '70') {
            this.severityData[i].name = '崩溃';
          } else {
            this.severityData[i].name = '宕机';
          }
        }
        this.drawPie('severityName', this.severityData, '严重程度');
      }
      if (res.reporter && res.reporter.length != 0) {
        this.reporterData = res.reporter;
        this.drawPie('reporterName', this.reporterData, '缺陷提交人');
      }
      if (res.handler && res.handler.length != 0) {
        this.handlerData = res.handler;
        this.drawPie('handlerName', this.handlerData, '修复人员');
      }
      if (res.redmine_id && res.redmine_id.length != 0) {
        this.unitData = res.redmine_id;
        this.drawPie('unitName', this.unitData, '功能模块分布');
      }
      if (res.status && res.status.length != 0) {
        this.statusData = res.status;
        let i = '';
        let len = '';
        for (i = 0, len = this.statusData.length; i < len; i++) {
          if (this.statusData[i].name == '10') {
            this.statusData[i].name = '新建';
          } else if (this.statusData[i].name == '20') {
            this.statusData[i].name = '拒绝';
          } else if (this.statusData[i].name == '30') {
            this.statusData[i].name = '确认拒绝';
          } else if (this.statusData[i].name == '40') {
            this.statusData[i].name = '延迟修复';
          } else if (this.statusData[i].name == '50') {
            this.statusData[i].name = '打开';
          } else if (this.statusData[i].name == '80') {
            this.statusData[i].name = '已修复';
          } else if (this.statusData[i].name == '90') {
            this.statusData[i].name = '关闭';
          }
        }
        this.drawPie('statusName', this.statusData, '缺陷状态');
      }
    },
    drawPie(pie, data, textName) {
      let alldata = data;
      let myPie = this.$echarts.init(document.getElementById('' + pie + ''));
      let i = '';
      let len = '';
      let typeArr = [];
      for (i = 0, len = alldata.length; i < len; i++) {
        typeArr.push(alldata[i].name);
      }
      const _this = this;
      myPie.setOption({
        title: {
          text: textName,
          x: 'center'
        },
        tooltip: {
          trrigger: 'item',
          formatter: '{a}<br/>{b}:{c}({d}%)'
        },
        legend: {
          orient: 'vertical',
          x: 'left',
          data: typeArr,
          formatter: function(name) {
            return _this.$echarts.format.truncateText(
              name,
              40,
              '14px Microsoft Yahei',
              '…'
            );
          },
          tooltip: {
            show: true
          }
        },
        calculable: true,
        series: [
          {
            name: '总数',
            type: 'pie',
            radius: '50%',
            center: ['52%', '45%'],
            avoidLabelOverlap: false,
            label: {
              normal: {
                show: true,
                position: 'inner',
                formatter: '{d}%'
              },
              emphasis: {
                show: true,
                textStyle: {
                  fontSize: '16',
                  fontWeight: 'bold'
                }
              }
            },
            data: alldata
          }
        ]
      });
    },
    handleEmailDialogOpen() {
      let orderType = this.orderDetail.orderType; //测试类型  安全测试  功能测试
      if (orderType == 'security') {
        this.isShowSafeTestMail = true;
      } else {
        this.emailDialogOpened = true;
        this.imageLink = this.orderDetail.imageLink;
      }
    },
    async sendEmail() {
      let orderType = this.orderDetail.orderType; //测试类型  安全测试  功能测试
      let params = {
        ...JSON.parse(sessionStorage.getItem('sitReportParams')),
        imageLink:
          orderType == 'security' ? this.wordDownLoadPath : this.imageLink
      };
      await this.sendSitDoneMail(params);
      this.$message({
        type: 'success',
        message: '邮件发送成功!'
      });
      this.emailDialogOpened = false;
      this.queryOrderByOrderNo({
        workNo: this.sitReportData.workOrderNo
      });
    },
    async uploadWord(file) {
      let length = file.file.name.split('.').length;
      let finalName = file.file.name.split('.')[length - 1];
      let size = file.file.size; //单位 M 后端限制20M
      if (finalName != 'docx') {
        this.$message({
          showClose: true,
          message: '只能选择.docx的文件,请重新选择!',
          type: 'error'
        });
        return;
      }
      if (size > 20971520) {
        this.$message({
          showClose: true,
          message: '文件大小不能超过20M,请修改文件!',
          type: 'error'
        });
        return;
      }
      let param = new FormData();
      param.append('file', file.file);
      param.append('fileType', '01');
      param.append('workNo', this.sitReportData.workOrderNo);
      let res = await uploadDocFile(param);
      this.wordDownLoadPath = decodeURIComponent(res.path);
      this.$message({
        showClose: true,
        message: '上传成功',
        type: 'success'
      });
    },
    saveFile(file, fileList) {
      if (fileList.length > 1) {
        fileList.splice(0, 1);
      }
    },
    printSitReport() {
      this.buttonShow = false;
      window.print();
    }
  },

  mounted() {
    const sitReportData = JSON.parse(sessionStorage.getItem('sitReportDataNo'));
    this.sitReportData = sitReportData;
    this.queryOrder();
    this.queryOrderByOrderNo({
      workNo: this.sitReportData.workOrderNo
    });
  }
};
</script>

<style scoped>
.container {
  width: 60%;
  margin: 0 auto;
  text-align: left;
  padding: 20px 0;
}
.item1 {
  width: 20%;
}
.tr-height {
  height: 40px;
}
.tablecss {
  width: 100%;
  border-color: #ebeef5;
}
td {
  padding-left: 15px;
  border: 1px solid #ebeef5;
}
.print-button {
  margin-top: 10px;
  margin-bottom: 20px;
}
.noprint {
  display: block;
}
@media print {
  .noprint {
    display: none;
  }
  .print {
    display: block;
  }
}
@page {
  margin: 0;
}
.chart-main {
  padding: 0px;
  margin-top: 10px;
  overflow: hidden;
}
.bar-main {
  padding: 0px;
}
.chart-frame {
  padding-top: 10px;
  height: 220px;
  width: 280px;
}
.bgcolor {
  background: #f5f7fa;
  font-weight: bold;
}
</style>
