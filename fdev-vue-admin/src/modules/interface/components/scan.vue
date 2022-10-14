<template>
  <f-block class="wrapper">
    <Page>
      <form @submit.prevent="submit" class="form q-mt-lg">
        <div class="row">
          <div class="col-sm-9 col-xs-12">
            <f-formitem page label="git项目地址">
              <fdev-input
                ref="interfaceScanModel.cloneUrl"
                v-model="$v.interfaceScanModel.cloneUrl.$model"
                type="text"
                class="input"
                placeholder="例如:http://xxx/ebank/CommonServices/user/ims-user-sim.git"
                dense
                :rules="[
                  () =>
                    $v.interfaceScanModel.cloneUrl.required ||
                    '请输入git项目地址',
                  () =>
                    $v.interfaceScanModel.cloneUrl.isUrlRegex ||
                    '请输入正确的git项目地址(Clone with HTTP)'
                ]"
              />
            </f-formitem>
            <f-formitem page label="分支名称">
              <fdev-input
                ref="interfaceScanModel.branchName"
                v-model="$v.interfaceScanModel.branchName.$model"
                type="text"
                class="input"
                placeholder="例如：master"
                dense
                :rules="[
                  () =>
                    $v.interfaceScanModel.branchName.required ||
                    '请输入分支名称'
                ]"
              />
            </f-formitem>
          </div>
        </div>
        <div class="row justify-center">
          <fdev-btn
            label="扫描"
            type="submit"
            dialog
            class="q-mr-md q-pl-lg q-pr-lg"
            :loading="loading"
          />
        </div>
      </form>
    </Page>
  </f-block>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import Page from '@/components/Page';
import { validate, resolveResponseError, successNotify } from '@/utils/utils';
import { scanInterface } from '@/services/interface';
export default {
  name: 'scan',
  components: { Page },
  data() {
    return {
      interfaceScanModel: {
        cloneUrl: '',
        branchName: ''
      },
      loading: false
    };
  },
  validations: {
    interfaceScanModel: {
      cloneUrl: {
        required,
        isUrlRegex(value) {
          if (!value) {
            return true;
          }
          let sRegex =
            '^((https|http|ftp|rtsp|mms)?://)' +
            "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" + // ftp的user@
            '(([0-9]{1,3}.){3}[0-9]{1,3}' + // IP形式的URL-xxx
            '|' + // 允许IP和DOMAIN（域名）
            "([0-9a-z_!~*'()-]+.)*" + // 域名- www.x`
            '([0-9a-z][0-9a-z-]{0,61})?[0-9a-z].' + // 二级域名
            '[a-z]{2,6})' + // first level domain- .com or .museum
            '(:[0-9]{1,4})?' + // 端口- :80
            '((/?)|' + // a slash isn't required if there is no file name
            "(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)" +
            '(.git)$';
          let re = new RegExp(sRegex);

          if (re.test(value)) {
            return true;
          }
          return false;
        }
      },
      branchName: {
        required
      }
    }
  },
  methods: {
    async submit() {
      this.$v.interfaceScanModel.$touch();
      let jobKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('interfaceScanModel') > -1
      );
      validate(jobKeys.map(key => this.$refs[key]));
      if (this.$v.interfaceScanModel.$invalid) {
        return;
      }
      this.loading = true;
      try {
        await resolveResponseError(() =>
          scanInterface(this.interfaceScanModel)
        );
      } catch (e) {
        this.loading = false;
        throw e;
      }
      this.loading = false;
      successNotify('扫描成功');
    }
  }
};
</script>
