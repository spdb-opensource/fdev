import Vue from 'vue';
import { errorNotify } from '@/utils/utils';
import './index.styl';
import { FileType } from '@/modules/Release/utils/model.js';

export default Vue.extend({
  data() {
    return {
      files: [],
      uploded: [],
      units: ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    };
  },

  props: {
    params: Object,
    filesKey: {
      type: String,
      default: 'files'
    },
    accept: String,
    multiple: Boolean,
    limit: Number,
    iconSize: {
      type: String,
      default: '32px'
    },
    draggable: Boolean,
    hideHeader: Boolean,
    hideBottom: Boolean,
    method: {
      type: Function,
      required: true
    },
    maxFileSize: [Number, String],
    maxTotalSize: [Number, String],
    uploadedFiles: Array,
    fileType: String
  },

  computed: {
    haveFiles() {
      return this.files.length > 0;
    },
    filesSizeByte() {
      return this.files.reduce((sum, file) => {
        return sum + file.size;
      }, 0);
    },
    filesSize() {
      return this.humanStorageSize(this.filesSizeByte);
    },
    limitSize() {
      return this.humanStorageSize(this.maxTotalSize);
    }
  },

  methods: {
    __stopWhile(title, message, ok = '继续', cancel = '取消') {
      return new Promise((resolve, reject) => {
        this.$q
          .dialog({
            title: title,
            message: message,
            ok: ok,
            cancel: cancel
          })
          .onOk(() => {
            resolve();
          })
          .onCancel(() => {
            reject();
          });
      });
    },
    async uploadFile({ files }, goOn) {
      files = Array.from(files);
      if (
        this.limit &&
        (files.length > this.limit ||
          files.length + this.files.length > this.limit)
      ) {
        errorNotify(
          `当前限制选择 ${this.limit} 个文件, 本次选择了 ${
            files.length
          } 个文件，共 ${files.length + this.files.length} 个文件。`
        );
        return;
      }
      if (goOn !== 'goOn') {
        this.files = [];
      }

      await this.handleFiles(files, 0);
      this.$emit('added', files);
    },

    openFiles(goOn) {
      const input = document.createElement('input');

      input.setAttribute('type', 'file');
      if (this.multiple) {
        input.setAttribute('multiple', 'multiple');
      }
      if (this.accept) {
        input.setAttribute('accept', this.accept);
      }
      input.onchange = file => this.uploadFile(input, goOn);
      input.click();
    },

    deleteFiles(file, key = 'name') {
      this.files = this.files.filter(item => item[key] !== file[key]);
      this.$emit('remove', file);
    },

    async handleSubmit() {
      const _formData = new FormData();
      if (this.params) {
        Object.keys(this.params).forEach(key => {
          _formData.append(key, this.params[key]);
        });
      }

      this.files.forEach(file => {
        _formData.append(this.filesKey, file);
      });

      if (this.maxTotalSize && this.filesSizeByte > this.maxTotalSize) {
        errorNotify(`上传大小不能超过${this.limitSize}`);
        return;
      }

      if (this.uploadedFiles) {
        let i = -1;
        let cancel = false;

        while (++i < this.files.length) {
          // '审核类-数据库审核材料'
          if (this.fileType === FileType['6']) {
            // 任务下已存在文件时提示，只能上传一个文件
            if (this.uploadedFiles.length > 0) {
              try {
                await this.__stopWhile(
                  '文件已存在',
                  `该任务下已存在文件，选择继续上传将为您覆盖该文件`,
                  '继续上传',
                  '取消上传'
                );
              } catch (e) {
                cancel = true;
                break;
              }
            }
          } else {
            const file = this.files[i];
            if (this.uploadedFiles.includes(file.name)) {
              // 任务下存在同名文件时提示该话术，可上传多个文件
              try {
                await this.__stopWhile(
                  '文件已存在',
                  `该任务下已存在"${
                    file.name
                  }"文件，选择继续上传将为您覆盖该任务`,
                  '继续上传',
                  '取消上传'
                );
              } catch (e) {
                cancel = true;
                break;
              }
            }
          }
        }

        if (cancel) {
          errorNotify('上传已取消!');
          return;
        }
      }

      this.$emit('beforeUpload', this.files, _formData);

      try {
        await this.method(_formData);
        this.uploded = [...this.uploded, ...this.files];
        this.files = [];
        this.$emit('success');
      } catch (e) {
        this.$emit('failure');
      } finally {
        this.$emit('finish');
      }
    },

    _handleList() {
      if (this.$scopedSlots.list) {
        return this.$scopedSlots.list({
          files: this.files,
          remove: this.deleteFiles
        });
      } else {
        return (
          <div class="file-wrapper q-pa-sm">
            <div
              v-show={!this.haveFiles && this.uploded.length < 1}
              class={this.draggable ? 'icon-drag' : ''}
            >
              <f-image
                name="cloud_upload"
                color="grey-5"
                size={this.iconSize}
                class="icon"
              />
              {this.draggable ? (
                <p class="text-grey-7 text-center">可将文件拖拽到此处</p>
              ) : (
                ''
              )}
            </div>
            {this.uploded.map(file => {
              return (
                <div class="file-item">
                  <div class="file">{file.name}</div>
                  <f-image
                    name="ion-md-checkmark"
                    class="cursor-pointer text-green"
                  />
                </div>
              );
            })}
            {this.files.map(file => {
              return (
                <div class="file-item">
                  <div class="file">{file.name}</div>
                  <f-image
                    name="close"
                    class="cursor-pointer"
                    {...{
                      on: {
                        click: () => this.deleteFiles(file)
                      }
                    }}
                  />
                </div>
              );
            })}
          </div>
        );
      }
    },

    _handleHeader(h) {
      if (this.hideHeader) return;

      if (this.$scopedSlots.header) {
        return this.$scopedSlots.header({
          totalSize: this.filesSize,
          limitSize: this.limitSize
        });
      }

      return h(
        'div',
        {
          class: 'q-px-md q-py-sm bg-primary upload-header text-white'
        },
        [
          h('p', { class: 'header-font' }, '上传文档'),
          h(
            'p',
            this.maxTotalSize
              ? this.filesSize + '/' + this.limitSize
              : this.filesSize
          )
        ]
      );
    },

    _handleFooter(h) {
      if (this.hideBottom) return;

      if (this.$scopedSlots.footer) {
        return this.$scopedSlots.footer({
          choose: this.openFiles,
          continueToChoose: () => this.openFiles('goOn'),
          confirm: this.handleSubmit
        });
      }

      return h(
        'fdev-btn-group',
        {
          class: 'full-width',
          props: {
            flat: true
          }
        },
        [
          h('fdev-btn', {
            class: 'full-width',
            props: {
              label: this.haveFiles ? '重新选择' : '选择文件',
              color: 'blue-6-xzwj'
            },
            on: {
              click: this.openFiles
            }
          }),
          h('fdev-btn', {
            class: 'full-width',
            props: {
              label: '继续选择',
              color: 'blue-6-jxxz'
            },
            on: {
              click: () => this.openFiles('goOn')
            },
            directives: [
              { name: 'show', value: this.haveFiles || this.uploded.length > 0 }
            ]
          }),
          h('fdev-btn', {
            class: 'full-width',
            props: {
              label: '确定',
              color: 'blue-6-confirm',
              disable: !this.haveFiles,
              type: 'submit'
            }
          })
        ]
      );
    },

    humanStorageSize(bytes) {
      let u = 0;

      while (parseInt(bytes, 10) >= 1024 && u < this.units.length - 1) {
        bytes /= 1024;
        ++u;
      }

      return `${bytes.toFixed(1)}${this.units[u]}`;
    },

    stopAndPrevent(e) {
      e.cancelable !== false && e.preventDefault();
      e.stopPropagation();
    },

    __onDrop(e) {
      this.stopAndPrevent(e);

      if (this.draggable) {
        this.uploadFile(e.dataTransfer, 'goOn');
      }
    },

    async handleFiles(files, i = 0) {
      while (i < files.length) {
        const file = files[i];
        const notExist = this.files.every(item => item.name !== file.name);

        if (this.maxFileSize && file.size > this.maxFileSize) {
          try {
            await this.__stopWhile(
              '文件超出大小',
              `${file.name} 体积大于 ${this.humanStorageSize(this.maxFileSize)}`
            );
            await this.handleFiles(files, ++i);
          } catch (e) {
            return;
          } finally {
            break;
          }
        }

        if (notExist) {
          this.files.push(file);
        }
        i++;
      }

      return;
    }
  },

  render(h) {
    return (
      <FdevForm
        class="upload bg-white"
        on={{
          submit: this.handleSubmit
        }}
      >
        {this._handleHeader(h)}

        <div
          class="upload-body"
          draggable
          on={{
            dragenter: this.stopAndPrevent,
            dragover: this.stopAndPrevent,
            dragleave: this.stopAndPrevent,
            drop: this.__onDrop
          }}
        >
          <div>{this.$slots.default}</div>

          {this._handleList()}
        </div>

        <div class="q-px-sm upload-footer">{this._handleFooter(h)}</div>
      </FdevForm>
    );
  }
});
