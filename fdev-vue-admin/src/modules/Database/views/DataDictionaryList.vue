<template>
  <f-block>
    <Loading :visible="showLoading">
      <fdev-table
        :data="tableData"
        :columns="isDBAWorker ? columns : columns.slice(0, columns.length - 1)"
        :pagination.sync="pagination"
        row-key="record_id"
        :visible-columns="visibleColumns"
        @request="changePagination"
        selection="multiple"
        :selected.sync="selected"
        @update:selected="selection"
        :onSelectCols="updatevisibleColumns"
        noExport
        title="数据字典"
        title-icon="list_s_f"
      >
        <template v-slot:top-right>
          <span>
            <fdev-tooltip v-if="canClick">{{ disabledTip }}</fdev-tooltip>
            <fdev-btn
              normal
              ficon="exit"
              label="批量导出"
              @click="handleDownload('xlsx', '批量')"
              :disable="canClick"
            />
          </span>
          <fdev-btn
            normal
            ficon="exit"
            label="全量导出"
            @click="handleDownload('xlsx', '全量')"
          />
          <fdev-btn
            ficon="download"
            normal
            label="下载模板"
            @click="handleDownloadFile"
          />
          <fdev-btn
            normal
            ficon="upload"
            label="批量导入"
            v-if="isDBAWorker"
            @click="handleImportDialogOpen"
          />
          <fdev-btn
            normal
            ficon="add"
            label="新增"
            v-if="isDBAWorker"
            @click="handleAddDictDialogOpen()"
          />
          <fdev-btn
            normal
            ficon="compile"
            label="生成表"
            v-if="dataList && isDBAWorker"
            @click="handleShowDataTable"
          />
        </template>
        <template v-slot:top-bottom>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="所属系统">
            <fdev-select
              :options="systemOption"
              option-label="system_name_cn"
              option-value="sys_id"
              map-options
              emit-value
              @filter="systemNameFilter"
              use-input
              @input="systemNameChange($event)"
              :value="dictionaryModel.sys_id"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.system_name_cn">
                      {{ scope.opt.system_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.database_name">
                      {{ scope.opt.database_name }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="数据库类型">
            <fdev-select
              :options="databaseType"
              option-label="appName_cn"
              :value="dictionaryModel.database_type"
              @input="updateDatabase_type($event)"
              use-input
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="数据库名">
            <span>
              <fdev-select
                :options="databaseNameOptions"
                option-label="database_name"
                @input="databaseNameChange($event)"
                :disable="canSelect"
                :value="dictionaryModel.database_name"
                use-input
              />
              <fdev-tooltip position="bottom" v-if="canSelect">{{
                canSelectTip
              }}</fdev-tooltip>
            </span>
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="字段英文名">
            <fdev-input
              :value="dictionaryModel.field_en_name"
              @input="updateName($event)"
              @keyup.enter.native="submitData"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="submitData"
                />
              </template>
            </fdev-input>
          </f-formitem>
        </template>
        <template v-slot:body-cell-system_name_cn="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.sysInfo.system_name_cn"
          >
            {{ props.row.sysInfo.system_name_cn || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.sysInfo.system_name_cn || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <template v-slot:body-cell-database_type="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.dataDict.database_type"
          >
            {{ props.row.dataDict.database_type || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-database_name="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.dataDict.database_name"
          >
            {{ props.row.dataDict.database_name || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-field_en_name="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.dataDict.field_en_name"
          >
            {{ props.row.dataDict.field_en_name || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-field_ch_name="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.dataDict.field_ch_name"
          >
            {{ props.row.dataDict.field_ch_name || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-field_type="props">
          <fdev-td class="text-ellipsis" :title="props.row.dataDict.field_type">
            {{ props.row.dataDict.field_type || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-field_length="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.dataDict.field_length"
          >
            {{ props.row.dataDict.field_length || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-list_field_desc="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-remark="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            {{ props.value }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-btn="props">
          <fdev-td class="justify-center">
            <fdev-btn
              label="修改"
              @click="handleUpdataDialogOpen('修改', props.row)"
              flat
              class="q-mr-sm"
            />
            <fdev-btn
              label="克隆"
              @click="handleUpdataDialogOpen('克隆', props.row)"
              flat
            />
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>

    <!-- 新增弹窗 -->
    <f-dialog title="新增" v-model="addDialogOpen" right>
      <f-formitem required diaS label="所属系统">
        <fdev-select
          :options="systemOption"
          @filter="systemNameFilter"
          use-input
          option-label="system_name_cn"
          option-value="sys_id"
          v-model="$v.dictionaryAddModel.sys_id.$model"
          @input="queryDatabaseName"
          ref="dictionaryAddModel.sys_id"
          map-options
          emit-value
          :rules="[
            () => $v.dictionaryAddModel.sys_id.required || '请选择所属系统'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.system_name_cn">
                  {{ scope.opt.system_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.database_name">
                  {{ scope.opt.database_name }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem required diaS label="数据库类型">
        <fdev-select
          :options="databaseType"
          @input="fieldTypeChange"
          option-label="database_type"
          ref="dictionaryAddModel.database_type"
          v-model="$v.dictionaryAddModel.database_type.$model"
          :rules="[
            () =>
              $v.dictionaryAddModel.database_type.required || '请选择数据库类型'
          ]"
        />
      </f-formitem>
      <f-formitem required diaS label="数据库名">
        <fdev-select
          :options="databaseNameOptions"
          option-label="database_name"
          ref="dictionaryAddModel.database_name"
          v-model="$v.dictionaryAddModel.database_name.$model"
          :rules="[
            () =>
              $v.dictionaryAddModel.database_name.required || '请选择数据库名'
          ]"
        >
          <template v-slot:no-option>
            <fdev-item>
              <fdev-item-section class="text-grey" :title="databaseNameTip">
                {{ databaseNameTip }}
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem required diaS label="字段类型">
        <fdev-select
          :options="fieldTypeAdd"
          ref="dictionaryAddModel.field_type"
          v-model="$v.dictionaryAddModel.field_type.$model"
          :rules="[
            () => $v.dictionaryAddModel.field_type.required || '请选择字段类型'
          ]"
        >
          <template v-slot:no-option>
            <fdev-item>
              <fdev-item-section class="text-grey" :title="fieldTypeTip">
                {{ fieldTypeTip }}
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem diaS label="字段长度">
        <fdev-input
          v-model="$v.dictionaryAddModel.field_length.$model"
          ref="dictionaryAddModel.field_length"
          class="q-pb-lg"
        />
      </f-formitem>
      <f-formitem required diaS label="是否为列表字段">
        <fdev-select
          :options="isListFieldOptions"
          ref="dictionaryAddModel.is_list_field"
          v-model="$v.dictionaryAddModel.is_list_field.$model"
          :rules="[
            () =>
              $v.dictionaryAddModel.is_list_field.required || '请选择列表字段'
          ]"
        />
      </f-formitem>
      <f-formitem required diaS label="字段英文名">
        <fdev-input
          v-model="$v.dictionaryAddModel.field_en_name.$model"
          ref="dictionaryAddModel.field_en_name"
          :rules="[
            () =>
              $v.dictionaryAddModel.field_en_name.required ||
              '请输入字段英文名',
            () =>
              $v.dictionaryAddModel.field_en_name.hasEnglish ||
              '只能字母开头且只能是字母、数字和特殊字符( _ )的结合'
          ]"
        />
      </f-formitem>
      <f-formitem required diaS label="字段中文名">
        <fdev-input
          v-model="$v.dictionaryAddModel.field_ch_name.$model"
          ref="dictionaryAddModel.field_ch_name"
          :rules="[
            () =>
              $v.dictionaryAddModel.field_ch_name.required ||
              '请输入字段中文名',
            () =>
              $v.dictionaryAddModel.field_ch_name.hasChinese ||
              '字段中文名必须有汉字'
          ]"
        />
      </f-formitem>
      <f-formitem
        :required="dictionaryAddModel.is_list_field === '否' ? false : true"
        diaS
        label="列表字段枚举说明"
      >
        <fdev-input
          placeholder="可选填字段格式：key1^^value1###key2^^value2"
          v-model="$v.dictionaryAddModel.list_field_desc.$model"
          ref="dictionaryAddModel.list_field_desc"
          type="textarea"
          :rules="[
            () =>
              $v.dictionaryAddModel.list_field_desc.hasRequired ||
              '请输入列表字段枚举说明'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="备注">
        <fdev-input
          v-model="$v.dictionaryAddModel.remark.$model"
          ref="dictionaryAddModel.remark"
          type="textarea"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" dialog outline @click="addDialogOpen = false" />
        <fdev-btn label="确定" dialog @click="handleAddDictDialog" />
      </template>
    </f-dialog>

    <!-- 修改/克隆弹窗 -->
    <f-dialog :title="title" v-model="updateDialogOpen" right>
      <f-formitem required diaS label="所属系统">
        <fdev-select
          readonly
          :options="systemNames"
          option-label="system_name_cn"
          option-value="sys_id"
          v-model="$v.dictionaryUpdateModel.sys_id.$model"
          @input="queryDatabaseName"
          ref="dictionaryUpdateModel.sys_id"
          map-options
          emit-value
          :rules="[() => true]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.system_name_cn">
                  {{ scope.opt.system_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.database_name">
                  {{ scope.opt.database_name }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem required diaS label="数据库类型">
        <fdev-select
          readonly
          :options="databaseType"
          option-label="database_type"
          ref="dictionaryUpdateModel.database_type"
          v-model="$v.dictionaryUpdateModel.database_type.$model"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.database_type.required ||
              '请选择数据库类型'
          ]"
        />
      </f-formitem>
      <f-formitem required diaS label="数据库名">
        <fdev-select
          readonly
          :options="databaseNameOptions"
          option-label="database_name"
          ref="dictionaryUpdateModel.database_name"
          v-model="$v.dictionaryUpdateModel.database_name.$model"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.database_name.required ||
              '请选择数据库名'
          ]"
        >
          <template v-slot:no-option>
            <fdev-item>
              <fdev-item-section class="text-grey" :title="databaseNameTip">
                {{ databaseNameTip }}
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem required diaS label="字段类型">
        <fdev-select
          :readonly="title == '修改' ? true : false"
          :options="fieldType"
          ref="dictionaryUpdateModel.field_type"
          v-model="$v.dictionaryUpdateModel.field_type.$model"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.field_type.required || '请选择字段类型'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="字段长度">
        <fdev-input
          v-model="$v.dictionaryUpdateModel.field_length.$model"
          ref="dictionaryUpdateModel.field_length"
          class="q-pb-lg"
        />
      </f-formitem>
      <f-formitem required diaS label="是否为列表字段">
        <fdev-select
          :options="isListFieldOptions"
          ref="dictionaryUpdateModel.is_list_field"
          v-model="$v.dictionaryUpdateModel.is_list_field.$model"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.is_list_field.required ||
              '请选择列表字段'
          ]"
        />
      </f-formitem>
      <f-formitem required diaS label="字段英文名">
        <fdev-input
          :readonly="title == '修改' ? true : false"
          v-model="$v.dictionaryUpdateModel.field_en_name.$model"
          ref="dictionaryUpdateModel.field_en_name"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.field_en_name.required ||
              '请输入字段英文名',
            () =>
              $v.dictionaryUpdateModel.field_en_name.hasEnglish ||
              '只能字母开头且只能是字母、数字和特殊字符( _ )的结合'
          ]"
        />
      </f-formitem>
      <f-formitem required diaS label="字段中文名">
        <fdev-input
          v-model="$v.dictionaryUpdateModel.field_ch_name.$model"
          ref="dictionaryUpdateModel.field_ch_name"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.field_ch_name.required ||
              '请输入字段中文名',
            () =>
              $v.dictionaryUpdateModel.field_ch_name.hasChinese ||
              '字段中文名必须有汉字'
          ]"
        />
      </f-formitem>
      <f-formitem
        :required="dictionaryUpdateModel.is_list_field === '否' ? false : true"
        diaS
        label="列表字段枚举说明"
      >
        <fdev-input
          placeholder="可选填字段格式：key1^^value1###key2^^value2"
          v-model="$v.dictionaryUpdateModel.list_field_desc.$model"
          ref="dictionaryUpdateModel.list_field_desc"
          type="textarea"
          :rules="[
            () =>
              $v.dictionaryUpdateModel.list_field_desc.hasRequired ||
              '请输入列表字段枚举说明'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="备注">
        <fdev-input
          v-model="$v.dictionaryUpdateModel.remark.$model"
          ref="dictionaryUpdateModel.remark"
          type="textarea"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          label="取消"
          dialog
          outline
          @click="updateDialogOpen = false"
        />
        <fdev-btn
          label="确定"
          dialog
          @click="handleUpdateAndCloneDictDialog"
          :loading="loading[`databaseForm/${sureLoading}`]"
        />
      </template>
    </f-dialog>

    <!-- 点击“生成表”的弹框 -->
    <f-dialog v-model="showDataTable" title="生成表" right>
      <f-formitem diaS required label="所属系统">
        <fdev-select
          :options="systemOption"
          option-label="system_name_cn"
          option-value="sys_id"
          map-options
          emit-value
          @filter="systemNameFilter"
          use-input
          @input="systemNameChange"
          ref="dictionaryModel.sys_id"
          v-model="dictionaryModel.sys_id"
          :rules="[
            () => $v.dictionaryModel.sys_id.required || '请选择所属系统'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.system_name_cn">
                  {{ scope.opt.system_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.database_name">
                  {{ scope.opt.database_name }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem diaS required label="库类型">
        <fdev-select
          :options="databaseType"
          option-label="appName_cn"
          ref="dictionaryModel.database_type"
          v-model="dictionaryModel.database_type"
          :rules="[
            () =>
              $v.dictionaryModel.database_type.required || '请选择数据库类型'
          ]"
        />
      </f-formitem>
      <f-formitem diaS required label="库名">
        <span>
          <fdev-tooltip v-if="!dictionaryModel.sys_id">{{
            canSelectTip
          }}</fdev-tooltip>
          <fdev-select
            :options="databaseNameOptions"
            option-label="database_name"
            @input="databaseNameChange"
            :disable="!dictionaryModel.sys_id"
            ref="dictionaryModel.database_name"
            v-model="dictionaryModel.database_name"
            :rules="[
              () => $v.dictionaryModel.database_name.required || '请选择库名'
            ]"
          />
        </span>
      </f-formitem>
      <f-formitem diaS required label="表名">
        <fdev-input
          ref="database_tableName"
          v-model="$v.database_tableName.$model"
          :rules="[() => $v.database_tableName.required || '请填写表名']"
        />
      </f-formitem>
      <f-formitem diaS label="备注">
        <fdev-input type="textarea" v-model="$v.database_remark.$model" />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" dialog outline @click="showDataTable = false" />
        <fdev-btn label="确定" dialog @click="handleCreateDataBase" />
      </template>
    </f-dialog>

    <!-- 批量导入弹窗 -->
    <f-dialog f-sc title="批量导入" v-model="importDialogOpen">
      <f-formitem label="已选文件">
        <div class="text-right">
          <p class="text-grey-7" v-show="importModel.files.length === 0">
            暂未选择文件
          </p>
          <div
            v-for="file in importModel.files"
            :key="file.name"
            class="q-pb-sm"
          >
            <p class="file-wrapper">{{ file.name }}</p>
            <f-icon
              name="close"
              @click="deleteFiles(file)"
              :width="14"
              :height="14"
            />
          </div>
        </div>
      </f-formitem>

      <div class="notice">
        注意事项：
        <ol>
          <li>上传文件必须为excel文件;</li>
        </ol>
      </div>
      <div class="row justify-center q-gutter-md">
        <fdev-btn
          :label="importModel.files.length > 0 ? '重新选择' : '选择文件'"
          @click="openFiles"
        />
        <fdev-btn
          label="继续选择"
          @click="openFiles('goOn')"
          v-show="importModel.files.length > 0"
        />
        <span>
          <fdev-btn
            label="确定"
            @click="uploadFiles"
            :disable="importModel.files.length === 0"
            :loading="loading[`databaseForm/impDictRecords`]"
          />
          <fdev-tooltip v-if="importModel.files.length === 0"
            >请先选择文件</fdev-tooltip
          >
        </span>
      </div>
    </f-dialog>
  </f-block>
</template>
<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify, errorNotify } from '@/utils/utils';
import {
  dictionaryAddListModel,
  importModel,
  dataDictionaryColumns
} from '@/modules/Database/utils/constants';
import { getPagination, setPagination } from '@/modules/Database/utils/setting';
export default {
  name: 'DataDictionaryList',
  components: {
    Loading
  },
  data() {
    return {
      systemOption: [],
      fieldTypeAdd: [],
      title: '',
      addDialogOpen: false,
      updateDialogOpen: false,
      importDialogOpen: false,
      showDataTable: false,
      disabledTip: '未选择系统',
      canSelectTip: '请先选择系统',
      canClick: true,
      dataList: false,
      canSelect: true,
      selected: [],
      databaseNameOptions: [],
      tableDataList: [],
      showLoading: false,
      dictionaryAddModel: dictionaryAddListModel(),
      dictionaryUpdateModel: dictionaryAddListModel(),
      importModel: importModel(),
      filterModel: {},
      pagination: {
        page: 1,
        rowsPerPage: getPagination().rowsPerPage,
        rowsNumber: 10
      },
      isListFieldOptions: ['是', '否'],
      columns: dataDictionaryColumns(),
      tableData: [],
      database_tableName: '',
      database_remark: ''
    };
  },
  validations: {
    dictionaryModel: {
      sys_id: {
        required
      },
      database_type: {
        required
      },
      database_name: {
        required
      }
    },
    database_tableName: {
      required
    },
    database_remark: {},
    dictionaryAddModel: {
      sys_id: {
        required
      },
      database_type: {
        required
      },
      database_name: {
        required
      },
      field_en_name: {
        required,
        hasEnglish(value) {
          let re = new RegExp(/^[a-zA-Z][a-zA-Z0-9_]*$/);
          return re.test(value);
        }
      },
      field_ch_name: {
        required,
        hasChinese(value) {
          if (!value) {
            return true;
          }
          let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          let flag = reg.test(value);
          return flag;
        }
      },
      field_type: {
        required
      },
      field_length: {},
      list_field_desc: {
        hasRequired(value) {
          if (this.dictionaryAddModel.is_list_field == '否') {
            return true;
          }
          return value;
        }
      },
      is_list_field: {
        required
      },
      remark: {}
    },
    dictionaryUpdateModel: {
      sys_id: {
        required
      },
      database_type: {
        required
      },
      database_name: {
        required
      },
      field_en_name: {
        required,
        hasEnglish(value) {
          let re = new RegExp(/^[a-zA-Z][a-zA-Z0-9_]*$/);
          return re.test(value);
        }
      },
      field_ch_name: {
        required,
        hasChinese(value) {
          if (!value) {
            return true;
          }
          let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          let flag = reg.test(value);
          return flag;
        }
      },
      field_type: {
        required
      },
      field_length: {},
      list_field_desc: {
        hasRequired(value) {
          if (this.dictionaryUpdateModel.is_list_field == '否') {
            return true;
          }
          return value;
        }
      },
      is_list_field: {
        required
      },
      remark: {}
    }
  },
  computed: {
    ...mapState('userActionSaveDatabase/DataDictionaryList', {
      dictionaryModel: 'dictionaryListModel',
      visibleColumns: 'visibleColumns'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('global', ['loading']),
    ...mapState('databaseForm', [
      'databaseType',
      'systemNames',
      'fieldType',
      'databaseNameById',
      'dictionaryList',
      'dictionaryListAll',
      'downTemplateFiles'
    ]),
    isDBAWorker() {
      return this.currentUser.role.some(
        item => item.label === 'DBA审核人' || item.label === '卡点管理员'
      );
    },
    databaseNameTip() {
      const { sys_id } = this.dictionaryAddModel || this.dictionaryUpdateModel;
      if (!sys_id) return '系统未选择!';
      return '暂无可选项';
    },
    fieldTypeTip() {
      const { database_type } = this.dictionaryAddModel;
      if (!database_type) return '数据库类型未选择!';
      return '暂无可选项';
    },
    sureLoading() {
      if (this.title === '修改') return 'updateDictRecord';
      return 'addDictRecord';
    }
  },
  watch: {
    'pagination.rowsPerPage': function(val) {
      setPagination({ rowsPerPage: val });
    },
    'dictionaryModel.sys_id': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    },
    'dictionaryModel.database_type': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    },
    'dictionaryModel.database_name': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    },
    'dictionaryModel.field_en_name': function(val) {
      if (val === '') {
        this.submitData();
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveDatabase/DataDictionaryList', [
      'updateSys_id',
      'updateDatabase_type',
      'updateDatabase_name',
      'updateName',
      'updatevisibleColumns'
    ]),
    ...mapActions('databaseForm', [
      'queryDbType',
      'querySystemNames',
      'queryDatabaseNameById',
      'queryDictRecord',
      'addDictRecord',
      'updateDictRecord',
      'impDictRecords',
      'queryFieldType',
      'queryDictRecordAll',
      'downloadTemplate',
      'addUseRecord'
    ]),
    // 系统名称过滤
    systemNameFilter(val, update) {
      update(() => {
        this.systemOption = this.systemNames.filter(
          model =>
            model.system_name_cn.indexOf(val) > -1 ||
            model.database_name.indexOf(val) > -1
        );
      });
    },
    // 提交‘生成表’的数据
    async handleCreateDataBase() {
      if (this.validationsAdd()) {
        let array = this.selected.map((val, index) => {
          return {
            field: val.dataDict.field_en_name,
            field_cn: val.dataDict.field_ch_name,
            field_type: val.dataDict.field_type,
            field_length: val.dataDict.field_length,
            is_list_field: this.selected[index]['is_list_field']
          };
        });
        let params = {
          sys_id: this.dictionaryModel.sys_id,
          database_type: this.dictionaryModel.database_type,
          database_name: this.dictionaryModel.database_name.database_name,
          table_name: this.database_tableName,
          all_field: array,
          is_new_table: 'Y',
          remark: this.database_remark
        };
        await this.addUseRecord(params);
        this.init();
        successNotify('生成表成功');
        this.showDataTable = false;
        this.dataList = false;
        if (this.dictionaryModel.sys_id) {
          this.dictionaryModel.sys_id = '';
        }
        if (this.dictionaryModel.database_type) {
          this.dictionaryModel.database_type = '';
        }
        if (this.dictionaryModel.database_name.database_name) {
          this.dictionaryModel.database_name.database_name = '';
        }
        if (this.database_tableName) {
          this.database_tableName = '';
        }
        if (this.database_remark) {
          this.database_remark = '';
        }
      }
    },
    async handleImportDialogOpen() {
      this.importModel = importModel();
      this.importDialogOpen = true;
    },
    // 点击”生成表“，弹出弹框
    async handleShowDataTable() {
      if (this.dictionaryModel.sys_id) {
        this.dictionaryModel.sys_id = '';
      }
      if (this.dictionaryModel.database_type) {
        this.dictionaryModel.database_type = '';
      }
      if (this.dictionaryModel.database_name.database_name) {
        this.dictionaryModel.database_name.database_name = '';
      }
      if (this.database_tableName) {
        this.database_tableName = '';
      }
      if (this.database_remark) {
        this.database_remark = '';
      }
      this.showDataTable = true;
    },
    openFiles(goOn) {
      const input = document.createElement('input');

      input.setAttribute('type', 'file');
      input.setAttribute('multiple', 'multiple');
      input.onchange = file => this.uploadFile(input, goOn);
      input.click();
    },
    uploadFile({ files }, goOn) {
      files = Array.from(files);
      if (goOn === 'goOn') {
        const modelFiles = [...this.importModel.files];

        files.forEach(file => {
          const notExist = this.importModel.files.every(
            item => item.name !== file.name
          );

          if (notExist) {
            modelFiles.push(file);
          }
        });

        this.importModel.files = modelFiles;
      } else {
        this.importModel.files = [...files];
      }
    },
    deleteFiles(file) {
      this.importModel.files = this.importModel.files.filter(
        item => item.name !== file.name
      );
    },
    async uploadFiles() {
      const formData = new FormData();
      this.importModel.files.forEach(file => {
        formData.append('file', file, file.name);
      });
      let flag = this.importModel.files.every(file => {
        if (
          file.name.substring(file.name.lastIndexOf('.')) === '.xls' ||
          file.name.substring(file.name.lastIndexOf('.')) === '.xlsx'
        ) {
          return true;
        } else {
          return false;
        }
      });
      if (flag) {
        await this.impDictRecords(formData);
        successNotify('上传成功！');
        this.importDialogOpen = false;
        this.init();
      } else {
        errorNotify('上传文件必须为excel文件,请重新选择文件');
      }
    },
    handleUpdateAllTip() {
      this.$v.dictionaryUpdateModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('dictionaryUpdateModel') > -1;
      });
      validate(Keys.map(key => this.$refs[key]));
      if (this.$v.dictionaryUpdateModel.$invalid) {
        let _this = this;
        let validateRes = Keys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.dictionaryUpdateModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      return true;
    },

    async handleAddDictDialogOpen() {
      this.addDialogOpen = true;
      this.fieldTypeAdd = [];
      if (this.dictionaryAddModel.sys_id) {
        this.dictionaryAddModel.sys_id = '';
      }
      if (this.dictionaryAddModel.database_type) {
        this.dictionaryAddModel.database_type = '';
      }
      if (this.dictionaryAddModel.database_name) {
        this.dictionaryAddModel.database_name = '';
      }
      if (this.dictionaryAddModel.field_en_name) {
        this.dictionaryAddModel.field_en_name = '';
      }
      if (this.dictionaryAddModel.field_ch_name) {
        this.dictionaryAddModel.field_ch_name = '';
      }
      if (this.dictionaryAddModel.field_type) {
        this.dictionaryAddModel.field_type = '';
      }
      if (this.dictionaryAddModel.field_length) {
        this.dictionaryAddModel.field_length = '';
      }
      if (this.dictionaryAddModel.list_field_desc) {
        this.dictionaryAddModel.list_field_desc = '';
      }
      if (this.dictionaryAddModel.is_list_field) {
        this.dictionaryAddModel.is_list_field = '';
      }
      if (this.dictionaryAddModel.remark) {
        this.dictionaryAddModel.remark = '';
      }
    },
    async handleAddDictDialog() {
      if (this.validations()) {
        delete this.dictionaryAddModel.record_id;
        delete this.dictionaryAddModel.system_name_cn;
        delete this.dictionaryAddModel.dict_id;
        await this.addDictRecord({
          ...this.dictionaryAddModel,
          database_name: this.dictionaryAddModel.database_name.database_name,
          is_list_field:
            this.dictionaryAddModel.is_list_field == '是' ? 'Y' : 'N'
        });
        this.addDialogOpen = false;
        this.init();
        successNotify('新增成功！');
      }
    },
    //修改 克隆
    async handleUpdataDialogOpen(title = '修改', row) {
      this.updateDialogOpen = true;
      this.title = title;
      if (this.title === '克隆') {
        await this.queryFieldType({
          fieldType: row.dataDict.database_type
        });
      }
      this.dictionaryUpdateModel = {
        ...row,
        ...row.sysInfo,
        ...row.dataDict,
        is_list_field: row.is_list_field == 'Y' ? '是' : '否'
      };
    },
    async handleUpdateAndCloneDictDialog() {
      if (this.handleUpdateAllTip()) {
        if (this.title === '修改') {
          await this.updateDictRecord({
            ...this.dictionaryUpdateModel,
            is_list_field:
              this.dictionaryUpdateModel.is_list_field == '是' ? 'Y' : 'N',
            system_name_cn: this.dictionaryUpdateModel.system_name_cn,
            dict_id: this.dictionaryUpdateModel.dict_id
          });
        } else {
          delete this.dictionaryUpdateModel.record_id;
          delete this.dictionaryUpdateModel.system_name_cn;
          delete this.dictionaryUpdateModel.dict_id;
          await this.addDictRecord({
            ...this.dictionaryUpdateModel,
            is_list_field:
              this.dictionaryUpdateModel.is_list_field == '是' ? 'Y' : 'N',
            system_name_cn: this.dictionaryUpdateModel.system_name_cn,
            dict_id: this.dictionaryUpdateModel.dict_id
          });
        }
        successNotify(`${this.title}成功`);
        this.init();
        this.updateDialogOpen = false;
      }
    },
    async querySystemNameList() {
      await this.querySystemNames({ sys_id: '' });
    },
    changePagination(props) {
      let { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.init();
    },
    // confirmToClose(key) {
    //   this.$q
    //     .dialog({
    //       title: '关闭弹窗',
    //       message: '关闭弹窗后数据将会丢失，确认要关闭？',
    //       cancel: true,
    //       persistent: true
    //     })
    //     .onOk(() => {
    //       this[key] = false;
    //     });
    // },
    async fieldTypeChange() {
      await this.queryFieldType({
        fieldType: this.dictionaryAddModel.database_type
      });
      this.fieldTypeAdd = this.fieldType;
    },
    async queryDatabaseName() {
      await this.queryDatabaseNameById({
        sys_id: this.dictionaryAddModel.sys_id
      });
      this.databaseNameOptions = this.databaseNameById;
    },

    selection(selected) {
      this.selected = selected;
      // 如果复选框中有数据被选中
      if (selected.length === 0) {
        this.canClick = true;
        this.dataList = false;
        this.disabledTip = '未选择系统';
      } else {
        this.canClick = false;
        this.dataList = true;
      }
    },
    validations() {
      this.$v.dictionaryAddModel.$touch();
      let Keys = Object.keys(this.$refs).filter(
        key => key.indexOf('dictionaryAddModel') > -1
      );
      validate(Keys.map(key => this.$refs[key]));

      if (this.$v.dictionaryAddModel.$invalid) {
        let _this = this;
        let validateRes = Keys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.dictionaryAddModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      return true;
    },
    validationsAdd() {
      this.$v.dictionaryModel.$touch();
      let Keys = Object.keys(this.$refs).filter(
        key =>
          key.indexOf('dictionaryModel') > -1 ||
          key.indexOf('database_tableName') > -1
      );
      validate(Keys.map(key => this.$refs[key]));

      if (this.$v.dictionaryModel.$invalid) {
        let _this = this;
        let validateRes = Keys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.dictionaryModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      return true;
    },
    async databaseNameChange($event) {
      this.updateDatabase_name($event);

      if (this.dictionaryModel.database_name == null) {
        this.dictionaryModel.database_name = '';
        this.dictionaryModel.sys_id = this.dictionaryModel.sys_id;
      }
    },
    async systemNameChange($event) {
      this.updateSys_id($event);

      this.dictionaryModel.database_type = '';
      this.dictionaryModel.database_name = '';
      if (
        this.dictionaryModel.sys_id != null &&
        this.dictionaryModel.sys_id != ''
      ) {
        await this.queryDatabaseNameById({
          sys_id: this.dictionaryModel.sys_id
        });
        this.canSelect = false;
        this.databaseNameOptions = this.databaseNameById;
      } else {
        this.canSelect = true;
        this.databaseNameOptions = [];
      }
    },
    async handleDownloadFile() {
      await this.downloadTemplate();
      let res = this.downTemplateFiles;
      let fileName = 'attachment;filename=template.xlsx';
      const fileType = res.headers['content-type'];
      fileName = fileName.substring(fileName.indexOf('=') + 1);
      const blob = new Blob([res.data], { type: fileType });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      URL.revokeObjectURL(url);
      document.body.removeChild(link);
      successNotify('模板下载成功');
    },
    async handleDownload(type, val) {
      if (val == '全量') {
        this.tableDataList = [];
        await this.queryDictRecordAll({
          page: this.pagination.page,
          per_page: 0,
          ...this.filterModel
        });
        this.tableDataList = this.dictionaryListAll.dbs;
        this.pagination.rowsNumber = this.dictionaryListAll.total;
      } else {
        this.tableDataList = this.selected;
      }
      let _this = this;
      _this.dowmloadLoading = true;
      import('@/utils/exportExcel').then(excel => {
        const tHeader = [
          '系统中文名',
          '数据库类型',
          '数据库名称',
          '字段英文名',
          '字段中文名',
          '字段类型',
          '字段长度',
          '是否列表字段',
          '列表字段枚举说明',
          '备注信息',
          '首次维护日期',
          '首次维护人英文名',
          '首次维护人姓名',
          '最后维护日期',
          '最后维护人英文名',
          '最后维护人姓名'
        ];
        const filterVal = [
          'system_name_cn',
          'database_type',
          'database_name',
          'field_en_name',
          'field_ch_name',
          'field_type',
          'field_length',
          'is_list_field',
          'list_field_desc',
          'remark',
          'first_modi_time',
          'first_modi_userNameEn',
          'first_modi_userName',
          'last_modi_time',
          'last_modi_userNameEn',
          'last_modi_userName'
        ];
        const data = _this.formatJson(filterVal);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: '数据字典',
          bookType: type
        });
        _this.dowmloadLoading = false;
        this.selected = [];
        this.canClick = true;
        this.init();
      });
    },
    formatJson(filterVal) {
      return this.tableDataList.map(row => {
        return filterVal.map(col => {
          let dataDict = [
            'database_type',
            'database_name',
            'field_en_name',
            'field_ch_name',
            'field_type',
            'field_length'
          ];
          if (col === 'system_name_cn') {
            return row.sysInfo.system_name_cn;
          } else if (dataDict.includes(col)) {
            return row.dataDict[col];
          } else {
            return row[col];
          }
        });
      });
    },
    async submitData() {
      this.filterModel = { ...this.dictionaryModel };
      this.init();
    },
    async init() {
      this.showLoading = true;
      const { database_name } = this.filterModel;
      await this.queryDictRecord({
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        ...this.filterModel,
        database_name: database_name.database_name
      });
      this.selected = [];
      // 当前页的数据信息
      this.tableData = this.dictionaryList.dbs;
      // 所有数据的条数
      this.pagination.rowsNumber = this.dictionaryList.total;
      this.showLoading = false;
    }
  },
  async created() {
    await this.querySystemNames({ sys_id: '' });
    await this.queryDbType();
    await this.submitData();
    if (
      this.dictionaryModel.sys_id !== null &&
      this.dictionaryModel.sys_id !== ''
    ) {
      this.canSelect = false;
      await this.queryDatabaseNameById({
        sys_id: this.dictionaryModel.sys_id
      });
      this.databaseNameOptions = this.databaseNameById;
    }
    this.systemOption = this.systemNames;
  }
};
</script>

<style lang="stylus" scoped>

.file-wrapper
  display inline-block
  width calc(100% - 20px)
  margin 0
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: bottom;
.notice
  color #757575
  ol
    margin 0
    padding-left 30px
    li
      line-height 20px
</style>
