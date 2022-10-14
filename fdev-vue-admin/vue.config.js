const path = require('path');
const yapiMocker = require('yapi-mocker');
const isDev = process.env.NODE_ENV === 'development';

module.exports = {
  pluginOptions: {
    quasar: {
      theme: 'mat',
      importAll: true
    }
  },
  lintOnSave: 'error',
  configureWebpack: config => {
    config.module.rules.push({
      test: path.resolve(__dirname, 'node_modules/leader-line/'),
      use: [
        {
          loader: 'skeleton-loader',
          options: {
            procedure: content => `${content}export default LeaderLine`
          }
        }
      ]
    });
  },
  chainWebpack: config => {
    config.plugin('html').tap(args => {
      args[0].favicon = isDev ? 'public/logo_dev.ico' : 'public/logo.ico';
      return args;
    });
    config.module
      .rule('eslint')
      .use('eslint-loader')
      .loader('eslint-loader')
      .tap(opt => {
        opt.emitWarning = opt.emitError = opt.failOnWarning = opt.failOnError = true;
        return opt;
      });
    config.resolve.alias.set(
      '#',
      path.resolve(__dirname, 'src/components/quasar/src')
    );
  },
  transpileDependencies: ['vue-echarts', 'resize-detector'],
  devServer: {
    before(app) {
      yapiMocker(app, {
        server: 'xxx',
        tokens: [
          '07e6749be40bf0d3e10aa3fa97c98148c963eab9c1dcb5d5dc49d1a28a471b45', //fdev-task
          'ace07e56915916136ca367ade8d847d77a86de5e71ba993c64731c2a855ca9e2', //fdev-release-v2.0
          'a72f74f3627e8cae95f7d210780b7a5714d76d68549bde54fb07a5ae1c3b2e21', //fdev-env-config
          '6f9737269c5f34185dd67be93828b2acb0d67259ef749969e3fe9c7370490307', //fdev-app
          'a62fc524735557facf486e2ad3277775511c5ab0610a396279b983e4e79c8b85', //fdev-interface
          '737d05725fc36b3739808c770df045b16fd86f2fb0926eeadda0298a3809e1a0', //fdev-user
          '3d27a5e41ec36aacd6c6d298baa5c04453f8cd510ff7852a0acbcd66854e5568', //fdev-notify
          'ecc8e180c5fc17b6c53b80e7e2d2ece360770cc0135da313a6f46a471a188d1b', //fdev-rqrm
          '740a9267efe8eb135c219536d6c59bddc657082d3684fd476f91c2c55e3d165a', // fdev-mantis
          '97cb8a6ecab8bd6b7bf76a4ec300a64b38e00dcbfdc7827a717f07055d727fa2', //fdev-component
          'dff39e0ffc2cd08baeb5fab8020d0d3802d6b2194f52f98271dbfbd1f4436298', //fdev-iams
          '5930ff04a2b6b8d73b14d0ea9feba458424a84e973d857ed5204955faec004d6', //fdev-demand
          'aade0f646af186a374d036a31ef98df1c1936e9cd887983214b1daddf5ce1899', // fwebhook
          '8393327373cef956b3332b8706a78c572fecc75aa2d5746926916af3a5d4fe61', // tuser
          '0b67416bac16b5d2d235168ca4e480d8d0066bae868b56dfdf0cb9a157de6688', // fdocmanage
          '53cc1efc3f547004af2f0bea3a68ac4f262383f0008b697a86fba44e1360a404', // torder
          'ef53a518a2c1af73386349b302b27a7dae45f355fc0d986872cfd0f2c56a2ffa', // fdatabase
          '3333b8c1ef2ac2c66ac3cc47b67f9646750f376809443609288512861256f1b7', // fsonar
          '3187d6c4e81c28b313ee4b31bc7739e4e222d2042dbd70a897af26ddbae842ed', // fgitwork
          'f3934fbee06b68c05972f6bf732710aef28ef3c2aaeaac8f1cc270c10d146cfc'
        ],
        watchFiles: path.resolve('./mock/index.js')
      });
    }
  },
  publicPath: process.env.NODE_ENV === 'production' ? './' : '/',
  productionSourceMap: true
};
